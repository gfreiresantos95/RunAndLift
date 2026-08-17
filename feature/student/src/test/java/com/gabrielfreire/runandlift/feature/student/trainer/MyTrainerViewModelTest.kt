package com.gabrielfreire.runandlift.feature.student.trainer

import com.gabrielfreire.runandlift.data.link.LinkRequestFailure
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository.Companion.CODE
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository.Companion.INVITE
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository.Companion.link
import com.gabrielfreire.runandlift.feature.student.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O caminho do código de convite, que é onde o vínculo nasce.
 *
 * O teste que mais importa é o dos **dois passos**: procurar o código não cria nada. Um resgate de
 * um passo só faria alguém autorizar a leitura da própria anamnese no toque seguinte a um erro de
 * digitação, e é justamente o tipo de passo que se remove "para simplificar".
 *
 * Os outros guardam as três respostas que não são "tente de novo": código que não existe, código do
 * próprio usuário, e vínculo que já existe.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyTrainerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `sem vinculo, a tela oferece o campo de codigo`() = runTest {
        val viewModel = viewModel(FakeLinkRepository())

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.current)
        assertTrue(viewModel.uiState.value.canEnterCode)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `com vinculo vigente, o campo de codigo nao aparece`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))))

        advanceUntilIdle()

        assertEquals(LinkStatus.ACTIVE, viewModel.uiState.value.current?.status)
        assertFalse(
            "trocar de treinador com um toque não é o que essa tela oferece",
            viewModel.uiState.value.canEnterCode,
        )
    }

    @Test
    fun `procurar o codigo nao cria vinculo nenhum`() = runTest {
        val links = FakeLinkRepository(invite = INVITE)
        val viewModel = viewModel(links)
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()

        // O convite encontrado é uma pergunta na tela, e não um pedido feito.
        assertEquals(INVITE, viewModel.uiState.value.invite)
        assertEquals(0, links.requestCount)
        assertNull(viewModel.uiState.value.current)
    }

    @Test
    fun `confirmar cria o pedido, pendente da resposta do treinador`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(invite = INVITE))
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()
        viewModel.onConfirmInvite()
        advanceUntilIdle()

        assertEquals(LinkStatus.REQUESTED, viewModel.uiState.value.current?.status)
        assertNull("o convite sai da tela depois de virar pedido", viewModel.uiState.value.invite)
        assertEquals("", viewModel.uiState.value.code)
    }

    @Test
    fun `o nome do aluno viaja para dentro do vinculo`() = runTest {
        val links = FakeLinkRepository(invite = INVITE)
        val viewModel = MyTrainerViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(displayName = "Ana Souza"),
            linkRepository = links,
        )
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()
        viewModel.onConfirmInvite()
        advanceUntilIdle()

        // Sem essa cópia, a carteira do treinador seria uma lista de identificadores: `users/{uid}`
        // é legível só pelo titular.
        assertEquals("Ana Souza", links.lastName)
    }

    @Test
    fun `codigo que nao existe manda conferir a digitacao`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(invite = INVITE))
        advanceUntilIdle()

        viewModel.onCodeChange("ZZZ999")
        viewModel.onSubmitCode()
        advanceUntilIdle()

        assertEquals(TrainerCodeError.NOT_FOUND, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.invite)
    }

    @Test
    fun `leitura que falha nao vira codigo inexistente`() = runTest {
        val links = FakeLinkRepository(invite = INVITE)
        val viewModel = viewModel(links)
        advanceUntilIdle()

        links.failReading = true
        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()

        // Mandar conferir a digitação de um código certo é o pior conselho possível.
        assertEquals(TrainerCodeError.UNKNOWN, viewModel.uiState.value.error)
    }

    @Test
    fun `cada recusa do repositorio vira a sua propria frase`() = runTest {
        // Quem decide a recusa é `LinkRequest`, no `:data`, com teste próprio. O que se afirma aqui
        // é a outra metade: cada motivo chega à tela como um erro distinto, e não como um genérico.
        val casos = mapOf(
            LinkRequestFailure.OWN_CODE to TrainerCodeError.OWN_CODE,
            LinkRequestFailure.ALREADY_LINKED to TrainerCodeError.ALREADY_LINKED,
            LinkRequestFailure.UNKNOWN to TrainerCodeError.UNKNOWN,
        )

        casos.forEach { (recusa, esperado) ->
            val viewModel = viewModel(FakeLinkRepository(invite = INVITE, requestFailure = recusa))
            advanceUntilIdle()

            viewModel.onCodeChange(CODE)
            viewModel.onSubmitCode()
            advanceUntilIdle()
            viewModel.onConfirmInvite()
            advanceUntilIdle()

            assertEquals(esperado, viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.current)
        }
    }

    @Test
    fun `o vinculo que a tela ja conhece vai junto do pedido`() = runTest {
        val encerrado = link(LinkStatus.ENDED)
        val links = FakeLinkRepository(links = listOf(encerrado), invite = INVITE)
        val viewModel = viewModel(links)
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()
        viewModel.onConfirmInvite()
        advanceUntilIdle()

        // É o que evita uma leitura que a regra nem permitiria: `links/{id}` inexistente volta como
        // permissão negada, e não como "não existe".
        assertEquals(encerrado, links.lastExisting)
    }

    @Test
    fun `vinculo encerrado e reaberto pelo mesmo codigo`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(links = listOf(link(LinkStatus.ENDED)), invite = INVITE))
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()
        viewModel.onConfirmInvite()
        advanceUntilIdle()

        assertEquals(LinkStatus.REQUESTED, viewModel.uiState.value.current?.status)
        assertTrue("reabrir escreve no mesmo documento", viewModel.uiState.value.past.isEmpty())
    }

    @Test
    fun `aceitar um convite recebido ativa o vinculo`() = runTest {
        val convite = link(LinkStatus.INVITED)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(convite)))
        advanceUntilIdle()

        viewModel.onStatusChange(convite, LinkStatus.ACTIVE)
        advanceUntilIdle()

        assertEquals(LinkStatus.ACTIVE, viewModel.uiState.value.current?.status)
    }

    @Test
    fun `encerrar e do aluno, e devolve a tela ao campo de codigo`() = runTest {
        val ativo = link(LinkStatus.ACTIVE)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(ativo)))
        advanceUntilIdle()

        viewModel.onStatusChange(ativo, LinkStatus.ENDED)
        advanceUntilIdle()

        // Quem revoga o acesso aos próprios dados de saúde não pede permissão a ninguém.
        assertNull(viewModel.uiState.value.current)
        assertEquals(1, viewModel.uiState.value.past.size)
        assertTrue(viewModel.uiState.value.canEnterCode)
    }

    @Test
    fun `o codigo sobe para maiuscula enquanto se digita`() = runTest {
        val viewModel = viewModel(FakeLinkRepository())
        advanceUntilIdle()

        viewModel.onCodeChange("abc234")

        // Ver `abc234` na tela enquanto o papel diz `ABC234` faz alguém apagar tudo achando que errou.
        assertEquals("ABC234", viewModel.uiState.value.code)
    }

    @Test
    fun `desistir do convite devolve o campo com o codigo digitado`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(invite = INVITE))
        advanceUntilIdle()

        viewModel.onCodeChange(CODE)
        viewModel.onSubmitCode()
        advanceUntilIdle()
        viewModel.onDismissInvite()

        assertNull(viewModel.uiState.value.invite)
        assertEquals(CODE, viewModel.uiState.value.code)
    }

    @Test
    fun `campo vazio nao procura nada`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(invite = INVITE))
        advanceUntilIdle()

        viewModel.onSubmitCode()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmitCode)
        assertNull(viewModel.uiState.value.error)
    }

    private fun viewModel(links: FakeLinkRepository) = MyTrainerViewModel(
        authRepository = FakeAuthRepository(),
        userRepository = FakeUserRepository(),
        linkRepository = links,
    )
}
