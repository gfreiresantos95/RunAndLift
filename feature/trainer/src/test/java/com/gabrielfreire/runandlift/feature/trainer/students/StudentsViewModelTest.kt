package com.gabrielfreire.runandlift.feature.trainer.students

import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository.Companion.link
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O que o preview da carteira não mostra: o que acontece quando a leitura falha, e o que sobra na
 * tela quando uma transição é recusada.
 *
 * O teste que mais importa é o da falha: **"você ainda não tem alunos" e "não consegui carregar" são
 * a mesma tela em branco**, e a primeira frase dita a um treinador com trinta alunos e sem sinal é
 * um susto — além de esconder que basta tentar de novo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `carteira lida aparece no estado`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))))

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.current.size)
        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel(FakeLinkRepository())

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `leitura que falha vira falha, e nao carteira vazia`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `a carteira que ja estava na tela sobrevive a uma releitura que falha`() = runTest {
        val links = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE)))
        val viewModel = viewModel(links)
        advanceUntilIdle()

        // A rede cai com a tela aberta, e a pessoa volta para a aba: quem já via a carteira não a
        // perde porque uma releitura não respondeu.
        links.failReading = true
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.links.size)
        assertTrue(viewModel.uiState.value.failed)
    }

    @Test
    fun `aceitar um pedido move o vinculo para ativo`() = runTest {
        val pedido = link(LinkStatus.REQUESTED)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(pedido)))
        advanceUntilIdle()

        viewModel.onStatusChange(pedido, LinkStatus.ACTIVE)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.pending.isEmpty())
        assertEquals(LinkStatus.ACTIVE, viewModel.uiState.value.current.single().status)
    }

    @Test
    fun `encerrar tira o aluno da lista de ativos sem apaga-lo`() = runTest {
        val ativo = link(LinkStatus.ACTIVE)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(ativo)))
        advanceUntilIdle()

        viewModel.onStatusChange(ativo, LinkStatus.ENDED)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.current.isEmpty())
        assertEquals(1, viewModel.uiState.value.past.size)
    }

    @Test
    fun `transicao recusada pelo servidor vira falha, e o estado nao muda`() = runTest {
        val ativo = link(LinkStatus.ACTIVE)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(ativo), failWriting = true))
        advanceUntilIdle()

        viewModel.onStatusChange(ativo, LinkStatus.ENDED)
        advanceUntilIdle()

        // Recarregar em vez de trocar o item em memória é o que impede a tela de mostrar um estado
        // que o banco não tem.
        assertTrue(viewModel.uiState.value.failed)
        assertEquals(LinkStatus.ACTIVE, viewModel.uiState.value.current.single().status)
    }

    @Test
    fun `uma gravacao por vez`() = runTest {
        val ativo = link(LinkStatus.ACTIVE)
        val viewModel = viewModel(FakeLinkRepository(links = listOf(ativo)))
        advanceUntilIdle()

        viewModel.onStatusChange(ativo, LinkStatus.PAUSED)
        // Sem esperar a primeira terminar: o segundo toque não pode disparar outra escrita.
        viewModel.onStatusChange(ativo, LinkStatus.ENDED)
        advanceUntilIdle()

        assertEquals(LinkStatus.PAUSED, viewModel.uiState.value.current.single().status)
    }

    @Test
    fun `sem sessao nao ha carteira, e a tela nao fica presa carregando`() = runTest {
        val viewModel = StudentsViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            linkRepository = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.loading)
    }

    private fun viewModel(links: FakeLinkRepository) =
        StudentsViewModel(authRepository = FakeAuthRepository(), linkRepository = links)
}
