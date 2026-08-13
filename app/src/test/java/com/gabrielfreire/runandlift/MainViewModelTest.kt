package com.gabrielfreire.runandlift

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.navigation.RoleRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * O destino inicial do app.
 *
 * São quatro desfechos e **a ordem entre eles é a regra**: sem sessão, sem papel, cadastro pela
 * metade, e só então a home. Trocar a ordem não quebra compilação nem tela — manda alguém para o
 * lugar errado na abertura, que é a primeira coisa que a pessoa vê.
 *
 * O terceiro caso é o que impede que **fechar o app** vire a forma de pular a conclusão de
 * cadastro, e é por isso que ele tem teste próprio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository(private val account: UserAccount?) : AuthRepository {
        override val currentAccount: Flow<UserAccount?> = flowOf(account)
        override fun currentAccountOrNull(): UserAccount? = account
        override suspend fun signUpWithEmail(email: String, password: String): AuthResult = AuthResult.Success(account)

        override suspend fun signInWithEmail(email: String, password: String): AuthResult = AuthResult.Success(account)

        override suspend fun signInWithGoogle(idToken: String): AuthResult = AuthResult.Success(account)
        override suspend fun sendPasswordReset(email: String): AuthResult = AuthResult.Success(account)
        override suspend fun sendEmailVerification(): AuthResult = AuthResult.Success(account)
        override suspend fun reloadAccount(): AuthResult = AuthResult.Success(account)
        override suspend fun signOut() = Unit
    }

    private class FakeUserRepository(
        private val storedProfile: UserProfile? = null,
        private val storedCref: String? = null,
        private val failReading: Boolean = false,
    ) : UserRepository {
        var activeRoleSetTo: ActiveRole? = null
            private set

        override suspend fun profile(uid: String): UserProfile? {
            if (failReading) error("sem rede e sem cache")
            return storedProfile
        }

        override suspend fun trainerRegistration(uid: String): String? {
            if (failReading) error("sem rede e sem cache")
            return storedCref
        }

        override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile =
            requireNotNull(storedProfile)

        override suspend fun setActiveRole(uid: String, role: ActiveRole) {
            activeRoleSetTo = role
        }
    }

    private val account = UserAccount(uid = "u1", email = "ana@exemplo.com", isEmailVerified = true)

    private fun completeProfile(roles: UserRoles = UserRoles(student = true)) = UserProfile(
        uid = "u1",
        displayName = "Ana Ribeiro",
        roles = roles,
        activeRole = ActiveRole.STUDENT,
        birthDate = LocalDate.of(1990, 5, 21),
        phone = "11987654321",
        acceptedTermsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
    )

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `sem sessao abre o fluxo de entrada`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(FakeAuthRepository(null), FakeUserRepository())
        testScheduler.advanceUntilIdle()

        assertEquals(AuthRoutes.GRAPH, viewModel.uiState.value.startDestination)
        assertTrue("a splash só sai depois de decidir", viewModel.uiState.value.ready)
    }

    @Test
    fun `com sessao e sem papel abre a escolha de papel`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(FakeAuthRepository(account), FakeUserRepository())
        testScheduler.advanceUntilIdle()

        // A conta existe mas não sabe o que é — sessão anterior à escolha, ou gravação que falhou.
        assertEquals(AuthRoutes.ROLE_SELECTION, viewModel.uiState.value.startDestination)
    }

    @Test
    fun `cadastro pela metade volta para a conclusao`() = runTest(testDispatcher) {
        val incomplete = completeProfile().copy(birthDate = null)
        val viewModel = MainViewModel(FakeAuthRepository(account), FakeUserRepository(incomplete))
        testScheduler.advanceUntilIdle()

        // Sem isto, fechar o aplicativo na tela de conclusão vira a forma de pular o que ela
        // pergunta — e a conta volta para a home sem nascimento e sem aceite dos termos.
        assertEquals(AuthRoutes.completeProfile(ActiveRole.STUDENT), viewModel.uiState.value.startDestination)
    }

    @Test
    fun `cadastro completo vai direto para o grafo do papel`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(FakeAuthRepository(account), FakeUserRepository(completeProfile()))
        testScheduler.advanceUntilIdle()

        assertEquals(RoleRoutes.STUDENT_GRAPH, viewModel.uiState.value.startDestination)
        assertEquals(ActiveRole.STUDENT, viewModel.uiState.value.activeRole)
    }

    @Test
    fun `leitura que falha nao segura ninguem na porta`() = runTest(testDispatcher) {
        val viewModel = MainViewModel(FakeAuthRepository(account), FakeUserRepository(failReading = true))
        testScheduler.advanceUntilIdle()

        // Sem perfil legível não há papel, então o desfecho é a escolha — e não um bloqueio na
        // conclusão de cadastro por um palpite.
        assertEquals(AuthRoutes.ROLE_SELECTION, viewModel.uiState.value.startDestination)
        assertTrue(viewModel.uiState.value.ready)
    }

    @Test
    fun `so quem tem os dois papeis pode alternar`() = runTest(testDispatcher) {
        val both = completeProfile(UserRoles(trainer = true, student = true))
        val viewModel = MainViewModel(FakeAuthRepository(account), FakeUserRepository(both))
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canSwitchRole)
    }

    @Test
    fun `quem tem um papel so nao alterna`() = runTest(testDispatcher) {
        val users = FakeUserRepository(completeProfile())
        val viewModel = MainViewModel(FakeAuthRepository(account), users)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSwitchRole)

        viewModel.switchRole { }
        testScheduler.advanceUntilIdle()

        assertEquals("sem os dois papéis não há o que alternar", null, users.activeRoleSetTo)
    }

    @Test
    fun `alternar grava o papel novo e avisa quem chamou`() = runTest(testDispatcher) {
        val both = completeProfile(UserRoles(trainer = true, student = true))
        val users = FakeUserRepository(both)
        val viewModel = MainViewModel(FakeAuthRepository(account), users)
        testScheduler.advanceUntilIdle()

        var switchedTo: ActiveRole? = null
        viewModel.switchRole { switchedTo = it }
        testScheduler.advanceUntilIdle()

        assertEquals(ActiveRole.TRAINER, users.activeRoleSetTo)
        assertEquals(ActiveRole.TRAINER, switchedTo)
        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.activeRole)
    }
}
