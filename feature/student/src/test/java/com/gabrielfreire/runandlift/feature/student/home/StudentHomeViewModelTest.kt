package com.gabrielfreire.runandlift.feature.student.home

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeStudentRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeTrainerRepository
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
import java.time.LocalDate
import java.time.ZoneId

/**
 * O que o preview da home não mostra: de onde vem o nome, e o que acontece quando ele não vem.
 *
 * As três formas de não haver nome são casos distintos e todas terminam na mesma tela utilizável —
 * é isso que se afirma aqui, porque a tentação de "tratar depois" costuma virar uma home presa em
 * carregamento para quem está sem rede.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `nome gravado aparece no estado`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(displayName = "Ana Ribeiro"))

        advanceUntilIdle()

        assertEquals("Ana Ribeiro", viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel()

        // Sem advanceUntilIdle de propósito: é o estado do primeiro frame, quando a home já está
        // desenhada e o nome ainda não chegou.
        assertTrue(viewModel.uiState.value.loading)
        assertNull(viewModel.uiState.value.displayName)
    }

    @Test
    fun `leitura que falha abre a home sem nome, e nao presa carregando`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(failReading = true))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `conta sem documento de perfil abre a home sem nome`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(missingProfile = true))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `sem sessao nao ha nome a mostrar`() = runTest {
        val viewModel = viewModel(auth = FakeAuthRepository(signedIn = null))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `nome em branco vale como ausente`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(displayName = "   "))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
    }

    @Test
    fun `o treinador da home e o do vinculo, e nao um exemplo`() = runTest {
        // Enquanto era exemplo, a home dizia a todo mundo que o treinador se chamava Marcos Vieira
        // — inclusive a quem não tinha treinador nenhum.
        val viewModel = viewModel(links = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))))

        advanceUntilIdle()

        val trainer = viewModel.uiState.value.trainer

        assertEquals("Marcos Vieira", trainer?.name)
        assertEquals(FakeTrainerRepository.CREF, trainer?.cref)
        assertEquals("09/03/2026", trainer?.sinceLabel)
    }

    @Test
    fun `sem vinculo nenhum a home nao inventa treinador`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.trainer)
    }

    @Test
    fun `vinculo pausado ainda e quem acompanha, encerrado nao`() = runTest {
        // Pausado é relação suspensa, não desfeita — a pessoa continua sendo o treinador dele.
        val pausado = viewModel(links = FakeLinkRepository(links = listOf(link(LinkStatus.PAUSED))))
        advanceUntilIdle()

        assertEquals("Marcos Vieira", pausado.uiState.value.trainer?.name)

        val encerrado = viewModel(links = FakeLinkRepository(links = listOf(link(LinkStatus.ENDED))))
        advanceUntilIdle()

        assertNull(encerrado.uiState.value.trainer)
    }

    @Test
    fun `pedido ainda nao aceito nao vira treinador na home`() = runTest {
        // A linha afirma que aquela pessoa acompanha este aluno. Dizer isso de quem ainda não
        // aceitou seria afirmar o que não é verdade — a tela "Meu treinador" é que mostra o pedido.
        val viewModel = viewModel(links = FakeLinkRepository(links = listOf(link(LinkStatus.REQUESTED))))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.trainer)
    }

    @Test
    fun `registro que nao chegou nao apaga o treinador da tela`() = runTest {
        // O CREF vem de um segundo documento. Esconder a pessoa inteira porque ele não respondeu
        // seria trocar uma informação parcial por nenhuma.
        val viewModel = viewModel(
            links = FakeLinkRepository(links = listOf(link(LinkStatus.ACTIVE))),
            trainers = FakeTrainerRepository(failReading = true),
        )

        advanceUntilIdle()

        assertEquals("Marcos Vieira", viewModel.uiState.value.trainer?.name)
        assertNull(viewModel.uiState.value.trainer?.cref)
    }

    @Test
    fun `leitura de vinculo que falha abre a home sem treinador`() = runTest {
        val viewModel = viewModel(links = FakeLinkRepository(failReading = true))

        advanceUntilIdle()

        assertNull("nomear um treinador por palpite é pior do que não nomear nenhum", viewModel.uiState.value.trainer)
        assertFalse(viewModel.uiState.value.loading)
    }

    /** O vínculo com data gravada, que é o que a home precisa para dizer "aluno desde". */
    private fun link(status: LinkStatus) = Link(
        trainerId = FakeTrainerRepository.TRAINER_ID,
        studentId = FakeAuthRepository.ACCOUNT.uid,
        status = status,
        origin = LinkOrigin.INVITE_CODE,
        trainerName = "Marcos Vieira",
        createdAt = LocalDate.of(2026, 3, 9)
            .atTime(12, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
    )

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
        students: FakeStudentRepository = FakeStudentRepository(),
        links: FakeLinkRepository = FakeLinkRepository(),
        trainers: FakeTrainerRepository = FakeTrainerRepository(),
    ) = StudentHomeViewModel(
        authRepository = auth,
        userRepository = users,
        studentRepository = students,
        linkRepository = links,
        trainerRepository = trainers,
    )
}
