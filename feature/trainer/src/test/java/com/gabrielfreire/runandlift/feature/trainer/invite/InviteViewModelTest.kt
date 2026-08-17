package com.gabrielfreire.runandlift.feature.trainer.invite

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository
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
 * O código de convite: quando ele existe, quando ele é criado, e o que a tela oferece nos dois casos.
 *
 * O primeiro teste guarda uma decisão fácil de desfazer sem perceber: **abrir a tela não gera
 * código**. Gerar é uma gravação em nome de quem talvez só tenha vindo olhar — e, para quem já tem
 * um código circulando, seria a troca dele sem que ninguém pedisse.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InviteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `abrir a tela sem codigo nao gera nenhum`() = runTest {
        val links = FakeLinkRepository()
        val viewModel = viewModel(links)

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.code)
        assertNull("gerar é decisão, e por isso tem botão", links.createdCode)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `codigo ja gravado aparece na tela`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(storedCode = "ABC234"))

        advanceUntilIdle()

        assertEquals("ABC234", viewModel.uiState.value.code)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `gerar cria o codigo e o mostra`() = runTest {
        val links = FakeLinkRepository()
        val viewModel = viewModel(links)
        advanceUntilIdle()

        viewModel.onGenerate()
        advanceUntilIdle()

        assertEquals("NEW234", viewModel.uiState.value.code)
        assertFalse(viewModel.uiState.value.working)
    }

    @Test
    fun `o nome do treinador viaja para dentro do convite`() = runTest {
        val links = FakeLinkRepository()
        val viewModel = InviteViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(displayName = "Carlos Pereira"),
            linkRepository = links,
        )
        advanceUntilIdle()

        viewModel.onGenerate()
        advanceUntilIdle()

        // É com ele que o aluno confere com quem vai se vincular antes de pedir.
        assertEquals("Carlos Pereira", links.lastName)
    }

    @Test
    fun `cadastro sem nome ainda gera codigo`() = runTest {
        val links = FakeLinkRepository()
        val viewModel = InviteViewModel(
            authRepository = FakeAuthRepository(),
            userRepository = FakeUserRepository(missingProfile = true),
            linkRepository = links,
        )
        advanceUntilIdle()

        viewModel.onGenerate()
        advanceUntilIdle()

        // Travar a criação por causa de um cadastro incompleto seria transformá-lo em porta fechada.
        assertEquals("NEW234", viewModel.uiState.value.code)
        assertEquals("", links.lastName)
    }

    @Test
    fun `gravacao que falha mantem o codigo antigo na tela`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(storedCode = "ABC234", failWriting = true))
        advanceUntilIdle()

        viewModel.onGenerate()
        advanceUntilIdle()

        // O código antigo continua valendo no banco: apagá-lo da tela faria o treinador achar que
        // não tem mais convite nenhum.
        assertEquals("ABC234", viewModel.uiState.value.code)
        assertTrue(viewModel.uiState.value.failed)
    }

    @Test
    fun `leitura que falha e falha, e nao ausencia de codigo`() = runTest {
        val viewModel = viewModel(FakeLinkRepository(storedCode = "ABC234", failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertNull(viewModel.uiState.value.code)
    }

    @Test
    fun `dois toques seguidos nao geram dois codigos`() = runTest {
        val links = FakeLinkRepository()
        val viewModel = viewModel(links)
        advanceUntilIdle()

        viewModel.onGenerate()
        // Sem esperar a primeira terminar: o segundo código apagaria o primeiro logo depois de ele
        // ter sido enviado a alguém.
        viewModel.onGenerate()
        advanceUntilIdle()

        assertEquals(1, links.createCount)
    }

    @Test
    fun `sem sessao a tela nao fica presa carregando`() = runTest {
        val viewModel = InviteViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            userRepository = FakeUserRepository(),
            linkRepository = FakeLinkRepository(storedCode = "ABC234"),
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertTrue(viewModel.uiState.value.failed)
    }

    private fun viewModel(links: FakeLinkRepository) = InviteViewModel(
        authRepository = FakeAuthRepository(),
        userRepository = FakeUserRepository(),
        linkRepository = links,
    )
}
