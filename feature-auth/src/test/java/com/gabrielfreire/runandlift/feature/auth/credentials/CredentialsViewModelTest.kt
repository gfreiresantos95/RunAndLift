package com.gabrielfreire.runandlift.feature.auth.credentials

import app.cash.turbine.test
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.BirthDateError
import com.gabrielfreire.runandlift.feature.auth.EmailError
import com.gabrielfreire.runandlift.feature.auth.NameError
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        var lastDetails: SignUpDetails? = null
            private set

        override suspend fun profile(uid: String): UserProfile? = storedRole?.let {
            UserProfile(
                uid = uid,
                displayName = null,
                roles = UserRoles(trainer = it == ActiveRole.TRAINER, student = it == ActiveRole.STUDENT),
                activeRole = it,
            )
        }

        override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile {
            if (failWriting) error("sem rede")
            lastDetails = details
            role?.let { rolesAdded = rolesAdded + it }
            return UserProfile(
                uid = uid,
                displayName = details.displayName,
                roles = UserRoles(trainer = role == ActiveRole.TRAINER, student = role == ActiveRole.STUDENT),
                activeRole = role,
                birthDate = details.birthDate,
                phone = details.phone,
            )
        }

        override suspend fun setActiveRole(uid: String, role: ActiveRole) = Unit
    }

    /** Preenche o cadastro inteiro com dados válidos — o que cada teste faz é desviar de um deles. */
    private fun SignUpViewModel.fillValidForm() {
        onNameChange("Ana Ribeiro")
        onEmailChange("valido@exemplo.com")
        onPasswordChange("senha123")
        onBirthDateChange("21051990")
        onTermsChange(true)
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

        viewModel.fillValidForm()
        viewModel.onPasswordChange("123")
        viewModel.onSubmit()

        assertEquals(PasswordError.TOO_SHORT, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `cadastro nao cria conta sem aceite dos termos`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())

        viewModel.fillValidForm()
        viewModel.onTermsChange(false)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue("aceite é condição para a conta existir", viewModel.formState.value.termsMissing)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `um envio revela todos os erros de uma vez`() = runTest(testDispatcher) {
        val viewModel = SignUpViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onSubmit()

        // Formulário que revela um erro por envio faz a pessoa tentar N vezes para descobrir N
        // coisas — a validação extra não pode ser curto-circuitada pela de credencial.
        assertEquals(EmailError.REQUIRED, viewModel.uiState.value.emailError)
        assertEquals(PasswordError.REQUIRED, viewModel.uiState.value.passwordError)
        assertEquals(NameError.REQUIRED, viewModel.formState.value.nameError)
        assertEquals(BirthDateError.REQUIRED, viewModel.formState.value.birthDateError)
        assertTrue(viewModel.formState.value.termsMissing)
    }

    @Test
    fun `cadastro recusa quem nao tem a idade minima`() = runTest(testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignUpViewModel(repository, FakeUserRepository())
        val tooYoung = LocalDate.now().minusYears(AuthFormValidation.MIN_AGE_YEARS - 1L)

        viewModel.fillValidForm()
        viewModel.onBirthDateChange(tooYoung.format(DateTimeFormatter.ofPattern("ddMMyyyy")))
        viewModel.onSubmit()

        assertEquals(BirthDateError.TOO_YOUNG, viewModel.formState.value.birthDateError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `cadastro grava o que o formulario coletou, inclusive o consentimento`() = runTest(testDispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.STUDENT)

        viewModel.fillValidForm()
        viewModel.onPhoneChange("11987654321")
        viewModel.onMarketingChange(true)
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        val details = users.lastDetails
        assertEquals("Ana Ribeiro", details?.displayName)
        assertEquals(LocalDate.of(1990, 5, 21), details?.birthDate)
        assertEquals("11987654321", details?.phone)
        assertEquals(PrivacyConsent.CURRENT_TERMS_VERSION, details?.consent?.termsVersion)
        assertEquals(true, details?.consent?.marketingOptIn)
    }

    @Test
    fun `opt-in de marketing e separado do aceite dos termos`() = runTest(testDispatcher) {
        val users = FakeUserRepository()
        val viewModel = SignUpViewModel(FakeAuthRepository(), users, ActiveRole.STUDENT)

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Consentimento em bloco não é consentimento: aceitar os termos não pode ligar o marketing.
        assertEquals(false, users.lastDetails?.consent?.marketingOptIn)
    }

    @Test
    fun `mascara nao chega ao estado, so digito`() = runTest(testDispatcher) {
        val viewModel = SignUpViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onBirthDateChange("21/05/1990")
        viewModel.onPhoneChange("(11) 98765-4321")

        assertEquals("21051990", viewModel.formState.value.birthDate)
        assertEquals("11987654321", viewModel.formState.value.phone)
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

        viewModel.fillValidForm()
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

        viewModel.fillValidForm()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertEquals(emptyList<ActiveRole>(), users.rolesAdded)
        assertNull(viewModel.uiState.value.resolvedRole)
        // Sem papel ainda, mas o consentimento não pode esperar pela tela seguinte: coletado e
        // não registrado é o mesmo que não ter coletado.
        assertNotNull(users.lastDetails?.consent)
    }

    @Test
    fun `falha ao gravar o papel nao derruba a conta recem-criada`() = runTest(testDispatcher) {
        val viewModel = SignUpViewModel(
            FakeAuthRepository(),
            FakeUserRepository(failWriting = true),
            ActiveRole.STUDENT,
        )

        viewModel.fillValidForm()
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
