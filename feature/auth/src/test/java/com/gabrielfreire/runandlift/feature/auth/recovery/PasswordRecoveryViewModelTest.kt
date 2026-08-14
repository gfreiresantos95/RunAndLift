package com.gabrielfreire.runandlift.feature.auth.recovery

import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.auth.validation.EmailError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Comportamento da recuperação de senha.
 *
 * O teste que importa é o primeiro: **a tela não pode revelar quem tem conta**. É uma regra de
 * privacidade, invisível na interface e fácil de desfazer sem querer — alguém "melhora" a mensagem
 * para dizer "e-mail não encontrado" e transforma a tela num verificador de base de usuários.
 */
class PasswordRecoveryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `e-mail que nao existe responde igual a e-mail que existe`() = runTest(mainDispatcherRule.dispatcher) {
        // O Firebase devolve credencial inválida quando não há conta com esse endereço.
        val repository = FakeAuthRepository(AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS))
        val viewModel = PasswordRecoveryViewModel(repository)

        viewModel.onEmailChange("naoexiste@exemplo.com")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue("distinguir os dois casos entrega quem tem conta no produto", viewModel.uiState.value.sent)
        assertNull("e não é falha: não há nada que a pessoa possa corrigir", viewModel.uiState.value.failure)
    }

    @Test
    fun `envio bem-sucedido confirma`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = PasswordRecoveryViewModel(FakeAuthRepository())

        viewModel.onEmailChange("ana@exemplo.com")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.sent)
        assertFalse(viewModel.uiState.value.submitting)
    }

    @Test
    fun `falha de rede e falha de verdade, e nao confirma o envio`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository(AuthResult.Failure(AuthFailure.NO_NETWORK))
        val viewModel = PasswordRecoveryViewModel(repository)

        viewModel.onEmailChange("ana@exemplo.com")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Sem rede, o link não saiu. Confirmar aqui faria a pessoa esperar por um e-mail que nunca
        // foi enviado — é o único caso em que tentar de novo muda alguma coisa.
        assertFalse(viewModel.uiState.value.sent)
        assertEquals(AuthFailure.NO_NETWORK, viewModel.uiState.value.failure)
    }

    @Test
    fun `nao chama a rede com e-mail invalido`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = PasswordRecoveryViewModel(repository)

        viewModel.onEmailChange("sem-arroba")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(EmailError.INVALID, viewModel.uiState.value.emailError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `apara o espaco que o teclado sugere antes de enviar`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = PasswordRecoveryViewModel(repository)

        viewModel.onEmailChange("ana@exemplo.com ")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // O teclado do Android sugere um espaço depois do domínio. Ele não pode chegar ao servidor,
        // ou o link vai para um endereço que não existe.
        assertEquals("ana@exemplo.com", repository.lastResetEmail)
    }

    @Test
    fun `voltar a digitar limpa a confirmacao anterior`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = PasswordRecoveryViewModel(FakeAuthRepository())

        viewModel.onEmailChange("ana@exemplo.com")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()
        viewModel.onEmailChange("bruno@exemplo.com")

        // A confirmação era sobre o endereço anterior. Mantê-la faria a tela afirmar que o link foi
        // enviado para um e-mail que ninguém submeteu.
        assertFalse(viewModel.uiState.value.sent)
    }

    @Test
    fun `envio duplicado nao dispara duas chamadas`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = PasswordRecoveryViewModel(repository)

        viewModel.onEmailChange("ana@exemplo.com")
        viewModel.onSubmit()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repository.calls)
    }
}
