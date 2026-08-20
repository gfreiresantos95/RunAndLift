package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeTrainerRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
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
 * O que o preview da home não mostra: de onde vem o nome, o que acontece quando ele não vem, e
 * quando o aviso de perfil incompleto aparece.
 *
 * As três formas de não haver nome são casos distintos e todas terminam na mesma tela utilizável —
 * é isso que se afirma aqui, porque a tentação de "tratar depois" costuma virar uma home presa em
 * carregamento para quem está sem rede.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainerHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `nome gravado aparece no estado`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(displayName = "Carlos Pereira"))

        advanceUntilIdle()

        assertEquals("Carlos Pereira", viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel()

        // Sem advanceUntilIdle de propósito: é o estado do primeiro frame, quando a home já está
        // desenhada e o nome ainda não chegou.
        assertTrue(viewModel.uiState.value.loading)
        assertNull(viewModel.uiState.value.displayName)
        assertFalse(
            "aviso que aparece antes da leitura sumiria um instante depois",
            viewModel.uiState.value.missing.any,
        )
    }

    @Test
    fun `perfil pela metade vira aviso na home`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.missing.any)
        assertTrue(viewModel.uiState.value.missing.specialties)
    }

    @Test
    fun `perfil completo nao mostra aviso`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(stored = FakeTrainerRepository.complete()))

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.missing.any)
    }

    @Test
    fun `leitura do perfil que falha nao inventa aviso`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(failReading = true))

        advanceUntilIdle()

        assertFalse(
            "acusar cadastro incompleto por palpite treina a pessoa a ignorar avisos",
            viewModel.uiState.value.missing.any,
        )
        assertFalse(viewModel.uiState.value.loading)
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
    fun `sem sessao nao ha nome nem aviso a mostrar`() = runTest {
        val viewModel = viewModel(auth = FakeAuthRepository(signedIn = null))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
        assertFalse(viewModel.uiState.value.missing.any)
        assertFalse(viewModel.uiState.value.loading)
        assertNull("sem sessão não há carteira para contar", viewModel.uiState.value.roster)
    }

    @Test
    fun `nome em branco vale como ausente`() = runTest {
        val viewModel = viewModel(users = FakeUserRepository(displayName = "   "))

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.displayName)
    }

    @Test
    fun `recarregar e o que faz o aviso sumir depois da edicao`() = runTest {
        val viewModel = viewModel(trainers = FakeTrainerRepository(stored = FakeTrainerRepository.complete()))
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.missing.any)
        assertEquals("Carlos Pereira", viewModel.uiState.value.displayName)
    }

    @Test
    fun `carteira lida vira contagem no painel`() = runTest {
        val viewModel = viewModel(
            links = FakeLinkRepository(
                links = listOf(
                    FakeLinkRepository.link(LinkStatus.ACTIVE, name = "Ana"),
                    FakeLinkRepository.link(LinkStatus.ACTIVE, name = "Bruno"),
                    FakeLinkRepository.link(LinkStatus.REQUESTED, name = "Carla"),
                    FakeLinkRepository.link(LinkStatus.ENDED, name = "Diego"),
                ),
            ),
        )

        advanceUntilIdle()

        val roster = viewModel.uiState.value.roster
        assertEquals(2, roster?.active)
        assertEquals(1, roster?.pending)
        assertEquals(1, roster?.ended)
    }

    @Test
    fun `carteira que falha vira nulo, e nao zero aluno`() = runTest {
        val viewModel = viewModel(links = FakeLinkRepository(failReading = true))

        advanceUntilIdle()

        assertNull(
            "dizer 0 alunos a quem tem trinta é informação errada, não palpite",
            viewModel.uiState.value.roster,
        )
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `carteira vazia de verdade conta zero, e nao vira falha`() = runTest {
        val viewModel = viewModel()

        advanceUntilIdle()

        val roster = viewModel.uiState.value.roster
        assertEquals(0, roster?.active)
        assertTrue("quem nunca teve aluno lê uma tela, quem não conseguiu ler lê outra", roster!!.isEmpty)
    }

    @Test
    fun `recarregar reconta a carteira depois de aceitar um pedido na outra aba`() = runTest {
        val links = FakeLinkRepository(links = listOf(FakeLinkRepository.link(LinkStatus.REQUESTED)))
        val viewModel = viewModel(links = links)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.roster?.active)

        links.updateStatus(FakeLinkRepository.link(LinkStatus.REQUESTED), LinkStatus.ACTIVE)
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.roster?.active)
    }

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
        trainers: FakeTrainerRepository = FakeTrainerRepository(),
        links: FakeLinkRepository = FakeLinkRepository(),
    ) = TrainerHomeViewModel(
        authRepository = auth,
        userRepository = users,
        trainerRepository = trainers,
        linkRepository = links,
    )
}
