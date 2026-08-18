package com.gabrielfreire.runandlift.feature.auth.credentials

import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInResult
import com.gabrielfreire.runandlift.feature.auth.signin.SignInViewModel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O que a folha do Google faz com o estado do formulário.
 *
 * O teste que mais importa é o do **cancelamento**: fechar a folha não é erro. A pessoa abriu, viu
 * as contas, decidiu que não era aquilo e voltou — pintar a tela de vermelho por isso trata uma
 * decisão dela como falha do app, e a manda procurar um problema que não existe.
 *
 * Os outros dois guardam o par que sustenta o resto: enquanto a folha está na frente, o formulário
 * fica bloqueado; e quando ela sai, o bloqueio sai junto — em qualquer um dos três desfechos.
 * Bloqueio que não é desfeito é a tela que "não faz mais nada" sem dizer por quê.
 *
 * Mora na pasta de `credentials/` e não na de `signin/` porque a regra é da base — entrar e criar
 * conta compartilham este caminho. O `SignInViewModel` entra como o concreto mais simples que a
 * herda: `CredentialsViewModel` é abstrato e a folha do Google não é oferecida no cadastro.
 */
class GoogleSignInStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `enquanto a folha esta na frente, o formulario fica bloqueado`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()

        viewModel.onGoogleSignInStarted()

        assertTrue(viewModel.uiState.value.submitting)
    }

    @Test
    fun `abrir a folha limpa a falha anterior`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onGoogleSignInResult(GoogleSignInResult.Failed(AuthFailure.NO_GOOGLE_ACCOUNT))

        viewModel.onGoogleSignInStarted()

        // Um erro da tentativa passada em cima da tentativa em curso é erro do que ainda não falhou.
        assertNull(viewModel.uiState.value.failure)
    }

    @Test
    fun `cancelar nao e erro, e nao pinta a tela de vermelho`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onGoogleSignInStarted()

        viewModel.onGoogleSignInResult(GoogleSignInResult.Cancelled)

        // O bloqueio sai, e nenhuma mensagem entra: a pessoa fechou a folha de propósito.
        assertFalse(viewModel.uiState.value.submitting)
        assertNull(viewModel.uiState.value.failure)
        assertFalse(viewModel.uiState.value.authenticated)
    }

    @Test
    fun `falha da folha vira mensagem, e libera o formulario`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        viewModel.onGoogleSignInStarted()

        viewModel.onGoogleSignInResult(GoogleSignInResult.Failed(AuthFailure.NO_GOOGLE_ACCOUNT))

        // Tem mensagem própria porque tem solução própria: adicionar uma conta no aparelho.
        assertEquals(AuthFailure.NO_GOOGLE_ACCOUNT, viewModel.uiState.value.failure)
        assertFalse(viewModel.uiState.value.submitting)
    }

    @Test
    fun `token autentica e resolve o papel da conta`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(
            authRepository = FakeAuthRepository(result = AuthResult.Success(FakeAuthRepository.ACCOUNT)),
            userRepository = FakeUserRepository(),
            intendedRole = ActiveRole.TRAINER,
        )
        viewModel.onGoogleSignInStarted()

        viewModel.onGoogleSignInResult(GoogleSignInResult.Token("id-token"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertFalse(viewModel.uiState.value.submitting)
    }

    @Test
    fun `token recusado pelo Firebase vira falha, e nao sessao`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(
            authRepository = FakeAuthRepository(result = AuthResult.Failure(AuthFailure.NO_NETWORK)),
            userRepository = FakeUserRepository(),
        )
        viewModel.onGoogleSignInStarted()

        viewModel.onGoogleSignInResult(GoogleSignInResult.Token("id-token"))
        advanceUntilIdle()

        // Obter o token e criar a sessão são dois passos; o segundo pode falhar sozinho.
        assertFalse(viewModel.uiState.value.authenticated)
        assertEquals(AuthFailure.NO_NETWORK, viewModel.uiState.value.failure)
        assertFalse(viewModel.uiState.value.submitting)
    }

    private fun viewModel() = SignInViewModel(FakeAuthRepository(), FakeUserRepository())
}
