package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
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
 * O que o preview da home não mostra: de onde vem o nome, e o que acontece quando ele não vem.
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

    private fun viewModel(
        auth: FakeAuthRepository = FakeAuthRepository(),
        users: FakeUserRepository = FakeUserRepository(),
    ) = TrainerHomeViewModel(authRepository = auth, userRepository = users)
}
