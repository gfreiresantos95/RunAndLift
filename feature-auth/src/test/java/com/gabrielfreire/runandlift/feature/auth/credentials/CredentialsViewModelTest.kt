package com.gabrielfreire.runandlift.feature.auth.credentials

import app.cash.turbine.test
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.user.UserRepository
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

    /**
     * @param storedRole papel que a conta já tem em `users/{uid}`.
     * @param failWriting simula a gravação do papel falhando com a conta já criada.
     */
    private class FakeUserRepository(
        private val storedRole: ActiveRole? = null,
        private val failWriting: Boolean = false,
    ) : UserRepository {
        var rolesAdded: List<ActiveRole> = emptyList()
            private set

        override suspend fun profile(uid: String): UserProfile? = storedRole?.let {
            UserProfile(
                uid = uid,
                displayName = null,
                roles = UserRoles(trainer = it == ActiveRole.TRAINER, student = it == ActiveRole.STUDENT),
                activeRole = it,
            )
        }

        override suspend fun addRole(uid: String, role: ActiveRole, displayName: String?): UserProfile {
            if (failWriting) error("sem rede")
            rolesAdded = rolesAdded + role
            return UserProfile(
                uid = uid,
                displayName = displayName,
                roles = UserRoles(trainer = role == ActiveRole.TRAINER, student = role == ActiveRole.STUDENT),
                activeRole = role,
            )
        }

        override suspend fun setActiveRole(uid: String, role: ActiveRole) = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `nao valida enquanto o usuario digita`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

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
        val viewModel = SignInViewModel(repository, FakeUserRepository())

        viewModel.onEmailChange("sem-arroba")
        viewModel.onSubmit()

        assertEquals(EmailError.INVALID, viewModel.uiState.value.emailError)
        assertEquals(PasswordError.REQUIRED, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `o erro some quando o usuario volta a digitar`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onEmailChange("invalido")
        viewModel.onSubmit()
        viewModel.onEmailChange("valido@exemplo.com")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `cadastro exige senha com o tamanho minimo`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()

        assertEquals(PasswordError.TOO_SHORT, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `entrada nao recusa senha curta, para nao revelar a regra a quem tem senha antiga`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignInViewModel(repository, FakeUserRepository())

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.passwordError)
        assertEquals(1, repository.calls)
    }

    @Test
    fun `sucesso marca autenticado e encerra o carregamento`() = runTest(testDispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

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
        val viewModel = SignInViewModel(repository, FakeUserRepository())

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
        val viewModel = SignInViewModel(repository, FakeUserRepository())

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals("toque duplo no botão não pode virar duas contas", 1, repository.calls)
    }

    @Test
    fun `cadastro grava o papel escolhido antes do login`() = runTest(testDispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.TRAINER)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(ActiveRole.TRAINER), users.rolesAdded)
        assertEquals(
            "com o papel já gravado, perguntar de novo seria perguntar duas vezes a mesma coisa",
            ActiveRole.TRAINER,
            viewModel.uiState.value.resolvedRole,
        )
    }

    @Test
    fun `cadastro sem escolha previa deixa o papel para a tela seguinte`() = runTest(testDispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, intendedRole = null)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertEquals(emptyList<ActiveRole>(), users.rolesAdded)
        assertNull(viewModel.uiState.value.resolvedRole)
    }

    @Test
    fun `falha ao gravar o papel nao derruba a conta recem-criada`() = runTest(testDispatcher) {
        val viewModel = SignUpViewModel(
            FakeAuthRepository(),
            FakeUserRepository(failWriting = true),
            ActiveRole.STUDENT,
        )

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // A conta já existe: devolver falha faria a pessoa tentar de novo e ouvir "e-mail em uso".
        assertTrue(viewModel.uiState.value.authenticated)
        assertNull(viewModel.uiState.value.failure)
        assertNull("sem papel gravado, a escolha acontece na tela seguinte", viewModel.uiState.value.resolvedRole)
    }

    @Test
    fun `entrar le o papel que a conta ja tem`() = runTest(testDispatcher) {
        val users = FakeUserRepository(storedRole = ActiveRole.TRAINER)
        val viewModel = SignInViewModel(FakeAuthRepository(), users)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.resolvedRole)
        assertEquals("entrar nunca grava papel", emptyList<ActiveRole>(), users.rolesAdded)
    }
}
