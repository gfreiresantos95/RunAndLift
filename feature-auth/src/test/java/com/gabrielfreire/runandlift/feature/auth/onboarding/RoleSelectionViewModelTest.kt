package com.gabrielfreire.runandlift.feature.auth.onboarding

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Escolha de papel depois de autenticar — a rede de segurança do fluxo.
 *
 * O que se afirma: confirmar só navega **depois** de a gravação dar certo, e sessão perdida é falha
 * de verdade. Um `confirmedRole` que espelhasse a seleção mandaria para o app alguém cujo papel não
 * chegou a ser gravado.
 */
class RoleSelectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository(signedIn = FakeAuthRepository.ACCOUNT)

    @Test
    fun `confirmar sem escolher nao faz nada`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = RoleSelectionViewModel(auth, users)

        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        // Nada a fazer e nada a sinalizar: confirmar o vazio não é uma ação, e não é um erro.
        assertEquals(emptyList<ActiveRole>(), users.rolesAdded)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `sem conta e falha de verdade`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = RoleSelectionViewModel(FakeAuthRepository(signedIn = null), FakeUserRepository())

        viewModel.onSelect(ActiveRole.STUDENT)
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        // Significa que a sessão caiu entre a tela anterior e esta.
        assertTrue(viewModel.uiState.value.failed)
    }

    @Test
    fun `grava o papel e so entao confirma`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = RoleSelectionViewModel(auth, users)

        viewModel.onSelect(ActiveRole.TRAINER)
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ActiveRole.TRAINER), users.rolesAdded)
        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.confirmedRole)
    }

    @Test
    fun `gravacao que falha nao navega`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = RoleSelectionViewModel(auth, FakeUserRepository(failWriting = true))

        viewModel.onSelect(ActiveRole.STUDENT)
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertNull("sem papel gravado, não há para onde mandar ninguém", viewModel.uiState.value.confirmedRole)
        assertFalse(viewModel.uiState.value.submitting)
    }

    @Test
    fun `escolher de novo limpa a falha anterior`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = RoleSelectionViewModel(auth, FakeUserRepository(failWriting = true))

        viewModel.onSelect(ActiveRole.STUDENT)
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()
        viewModel.onSelect(ActiveRole.TRAINER)

        assertFalse(viewModel.uiState.value.failed)
        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.selected)
    }

    @Test
    fun `deriva um nome do e-mail para a conta que ainda nao tem`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = RoleSelectionViewModel(auth, users)

        viewModel.onSelect(ActiveRole.STUDENT)
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        // O repositório preserva o nome real de quem passou pelo formulário; este só entra quando
        // não há nenhum.
        assertEquals("a", users.lastDetails?.displayName)
    }

    @Test
    fun `confirmacao duplicada nao grava duas vezes`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository()
        val viewModel = RoleSelectionViewModel(auth, users)

        viewModel.onSelect(ActiveRole.STUDENT)
        viewModel.onConfirm()
        viewModel.onConfirm()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ActiveRole.STUDENT), users.rolesAdded)
    }
}
