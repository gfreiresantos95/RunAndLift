package com.gabrielfreire.runandlift.feature.auth.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Recuperação de senha (backlog E1-10).
 *
 * A confirmação é deliberadamente ambígua — "se o e-mail estiver cadastrado" — e o resultado é
 * [PasswordRecoveryUiState.sent] mesmo quando o endereço não existe. Mensagem que distingue os dois
 * casos transforma a tela em um verificador de quem tem conta no produto.
 *
 * Não herda de `CredentialsViewModel`: o que ela compartilha com entrar e cadastrar é um campo de
 * e-mail, e o resto diverge inteiro — não há senha, não há papel a resolver, e "deu certo" aqui não
 * significa que alguém entrou.
 */
internal class PasswordRecoveryViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, failure = null, sent = false) }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.submitting) return

        val emailError = AuthFormValidation.validateEmail(current.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        _uiState.update { it.copy(submitting = true, failure = null) }

        viewModelScope.launch {
            when (val result = authRepository.sendPasswordReset(current.email.trim())) {
                is AuthResult.Success -> _uiState.update { it.copy(submitting = false, sent = true) }
                is AuthResult.Failure -> onRecoveryFailure(result.reason)
            }
        }
    }

    /**
     * Credencial inválida aqui significa "não existe conta com esse e-mail". Tratado como sucesso,
     * pelo mesmo motivo da ambiguidade acima. Falha de rede continua sendo falha de verdade.
     */
    private fun onRecoveryFailure(reason: AuthFailure) {
        val isAccountNotFound = reason == AuthFailure.INVALID_CREDENTIALS
        _uiState.update {
            it.copy(
                submitting = false,
                sent = isAccountNotFound,
                failure = reason.takeUnless { isAccountNotFound },
            )
        }
    }
}
