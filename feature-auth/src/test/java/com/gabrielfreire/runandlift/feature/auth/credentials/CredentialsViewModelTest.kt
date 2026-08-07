package com.gabrielfreire.runandlift.feature.auth.credentials

import app.cash.turbine.test
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.feature.auth.EmailError
import com.gabrielfreire.runandlift.feature.auth.PasswordError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comportamento do formulário de credenciais.
 *
 * O que se afirma aqui é a regra de quando validar e o que fazer com a falha — não a aparência da
 * tela. `Dispatchers.setMain` é necessário porque `viewModelScope` roda na Main.
 */
class CredentialsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository(private val result: AuthResult = AuthResult.Success(ACCOUNT)) : AuthRepository {
        var calls: Int = 0
            private set

        override val currentAccount: Flow<UserAccount?> = flowOf(null)
        override fun currentAccountOrNull(): UserAccount? = null

        override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
            calls++
            return result
        }

        override suspend fun signInWithEmail(email: String, password: String): AuthResult {
            calls++
            return result
        }

        override suspend fun signInWithGoogle(idToken: String): AuthResult = result
        override suspend fun sendPasswordReset(email: String): AuthResult = result
        override suspend fun sendEmailVerification(): AuthResult = result
        override suspend fun reloadAccount(): AuthResult = result
        override suspend fun signOut() = Unit

        companion object {
            val ACCOUNT = UserAccount(uid = "u1", email = "a@b.com", isEmailVerified = false)
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `nao valida enquanto o usuario digita`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository())

        viewModel.onEmailChange("a")

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull("erro durante a digitação atrapalha em vez de ajudar", state.emailError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valida so no envio e nao chama a rede com formulario invalido`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChange("sem-arroba")
        viewModel.onSubmit()

        assertEquals(EmailError.INVALID, viewModel.uiState.value.emailError)
        assertEquals(PasswordError.REQUIRED, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `o erro some quando o usuario volta a digitar`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository())

        viewModel.onEmailChange("invalido")
        viewModel.onSubmit()
        viewModel.onEmailChange("valido@exemplo.com")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `cadastro exige senha com o tamanho minimo`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()

        assertEquals(PasswordError.TOO_SHORT, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `entrada nao recusa senha curta, para nao revelar a regra a quem tem senha antiga`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.passwordError)
        assertEquals(1, repository.calls)
    }

    @Test
    fun `sucesso marca autenticado e encerra o carregamento`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository())

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertEquals(false, viewModel.uiState.value.submitting)
    }

    @Test
    fun `falha expoe o motivo e nao autentica`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS))
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha-errada")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(AuthFailure.INVALID_CREDENTIALS, viewModel.uiState.value.failure)
        assertEquals(false, viewModel.uiState.value.authenticated)
    }

    @Test
    fun `envio duplicado nao dispara duas chamadas`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignInViewModel(repository)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals("toque duplo no botão não pode virar duas contas", 1, repository.calls)
    }
}
