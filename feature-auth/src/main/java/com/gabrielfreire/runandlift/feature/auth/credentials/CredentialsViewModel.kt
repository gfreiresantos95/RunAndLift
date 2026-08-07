package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.EmailError
import com.gabrielfreire.runandlift.feature.auth.PasswordError
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado do formulário de e-mail e senha. Sem tipo de UI e sem recurso de string. */
internal data class CredentialsUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val failure: AuthFailure? = null,
    val submitting: Boolean = false,
    val authenticated: Boolean = false,
)

/**
 * Base de entrar e criar conta: o formulário é o mesmo, muda o que o botão faz.
 *
 * O erro de campo só aparece **depois** da primeira tentativa de envio, e some assim que o usuário
 * volta a digitar. Validar a cada tecla acusa "e-mail inválido" enquanto a pessoa ainda está no
 * meio do endereço, o que atrapalha em vez de ajudar.
 *
 * @param requireStrongPassword `true` no cadastro, onde o comprimento mínimo vale; `false` ao
 *   entrar, onde recusar por tamanho apenas revelaria a regra a quem já tem senha antiga.
 */
internal abstract class CredentialsViewModel(
    private val requireStrongPassword: Boolean,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CredentialsUiState())
    val uiState: StateFlow<CredentialsUiState> = _uiState.asStateFlow()

    protected abstract suspend fun authenticate(email: String, password: String): AuthResult

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, failure = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, failure = null) }
    }

    /** A folha do Google abriu. Bloqueia o formulário enquanto ela estiver na frente. */
    fun onGoogleSignInStarted() {
        _uiState.update { it.copy(submitting = true, failure = null) }
    }

    /**
     * Desfecho da folha do Google.
     *
     * Cancelamento **não** é erro: o usuário fechou a folha de propósito, e pintar a tela de
     * vermelho por isso trata uma decisão dele como falha do app.
     */
    fun onGoogleSignInResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Cancelled ->
                _uiState.update { it.copy(submitting = false) }

            is GoogleSignInResult.Failed ->
                _uiState.update { it.copy(submitting = false, failure = result.reason) }

            is GoogleSignInResult.Token -> authenticateWithGoogle(result.idToken)
        }
    }

    private fun authenticateWithGoogle(idToken: String) {
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(submitting = false, authenticated = true) }

                is AuthResult.Failure ->
                    _uiState.update { it.copy(submitting = false, failure = result.reason) }
            }
        }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.submitting) return

        val emailError = AuthFormValidation.validateEmail(current.email)
        val passwordError = AuthFormValidation.validatePassword(
            password = current.password,
            requireMinLength = requireStrongPassword,
        )

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(submitting = true, failure = null) }

        viewModelScope.launch {
            when (val result = authenticate(current.email, current.password)) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(submitting = false, authenticated = true) }

                is AuthResult.Failure ->
                    _uiState.update { it.copy(submitting = false, failure = result.reason) }
            }
        }
    }
}

internal class SignInViewModel(private val authRepository: AuthRepository) :
    CredentialsViewModel(requireStrongPassword = false, authRepository = authRepository) {

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signInWithEmail(email = email, password = password)
}

internal class SignUpViewModel(private val authRepository: AuthRepository) :
    CredentialsViewModel(requireStrongPassword = true, authRepository = authRepository) {

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signUpWithEmail(email = email, password = password)
}
