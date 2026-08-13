package com.gabrielfreire.runandlift.feature.auth.signin

import app.cash.turbine.test
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.feature.auth.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.auth.fake.FakeUserRepository
import com.gabrielfreire.runandlift.feature.auth.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.auth.validation.EmailError
import com.gabrielfreire.runandlift.feature.auth.validation.PasswordError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Comportamento da entrada.
 *
 * O que se afirma aqui é a regra de quando validar, o que fazer com a falha e **o que a entrada
 * nunca faz**: gravar papel. A aparência da tela não é testada — o projeto não tem teste de UI, e
 * o que se confere olhando são os previews.
 */
class SignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `nao valida enquanto o usuario digita`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onEmailChange("a")

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull("erro durante a digitação atrapalha em vez de ajudar", state.emailError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `valida so no envio e nao chama a rede com formulario invalido`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = SignInViewModel(repository, FakeUserRepository())

        viewModel.onEmailChange("sem-arroba")
        viewModel.onSubmit()

        assertEquals(EmailError.INVALID, viewModel.uiState.value.emailError)
        assertEquals(PasswordError.REQUIRED, viewModel.uiState.value.passwordError)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `o erro some quando o usuario volta a digitar`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onEmailChange("invalido")
        viewModel.onSubmit()
        viewModel.onEmailChange("valido@exemplo.com")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `nao recusa senha curta, para nao revelar a regra a quem tem senha antiga`() =
        runTest(mainDispatcherRule.dispatcher) {
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
    fun `sucesso marca autenticado e encerra o carregamento`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository())

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.authenticated)
        assertEquals(false, viewModel.uiState.value.submitting)
    }

    @Test
    fun `falha expoe o motivo e nao autentica`() = runTest(mainDispatcherRule.dispatcher) {
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
    fun `envio duplicado nao dispara duas chamadas`() = runTest(mainDispatcherRule.dispatcher) {
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
    fun `le o papel que a conta ja tem e nunca grava papel`() = runTest(mainDispatcherRule.dispatcher) {
        val users = FakeUserRepository(storedRole = ActiveRole.TRAINER)
        val viewModel = SignInViewModel(FakeAuthRepository(), users)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha123")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(ActiveRole.TRAINER, viewModel.uiState.value.resolvedRole)
        assertEquals("entrar nunca grava papel", emptyList<ActiveRole>(), users.rolesAdded)
    }

    @Test
    fun `conta sem nascimento nem aceite manda concluir o cadastro`() = runTest(mainDispatcherRule.dispatcher) {
        // É o retrato de quem entrou pela folha do Google: papel escolhido na abertura, conta
        // autenticada, e nenhum dos dados que só a pessoa tem como informar.
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository(), ActiveRole.TRAINER)

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha1234")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileIncomplete)
        assertEquals(
            "sem papel gravado, vale o que foi escolhido nas boas-vindas",
            ActiveRole.TRAINER,
            viewModel.uiState.value.resolvedRole,
        )
    }

    @Test
    fun `cadastro completo vai direto para o app`() = runTest(mainDispatcherRule.dispatcher) {
        val complete = UserProfile(
            uid = "u1",
            displayName = "Ana Ribeiro",
            roles = UserRoles(student = true),
            activeRole = ActiveRole.STUDENT,
            birthDate = LocalDate.of(1990, 5, 21),
            acceptedTermsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
        )
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository(storedProfile = complete))

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha1234")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.profileIncomplete)
    }

    @Test
    fun `leitura de perfil que falha nao trava a entrada`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = SignInViewModel(FakeAuthRepository(), FakeUserRepository(failReading = true))

        viewModel.onEmailChange("valido@exemplo.com")
        viewModel.onPasswordChange("senha1234")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        // Sem rede e sem cache não dá para afirmar nada sobre o perfil. A entrada segue: o papel
        // desconhecido cai na tela de escolha, e o cadastro é dado por completo — prender quem só
        // quer treinar por causa de um palpite é pior do que deixar passar.
        assertTrue(viewModel.uiState.value.authenticated)
        assertNull(viewModel.uiState.value.resolvedRole)
        assertEquals(false, viewModel.uiState.value.profileIncomplete)
    }
}
