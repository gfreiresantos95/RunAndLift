package com.gabrielfreire.runandlift.feature.auth.recovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.EmailError
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class PasswordRecoveryUiState(
    val email: String = "",
    val emailError: EmailError? = null,
    val failure: AuthFailure? = null,
    val submitting: Boolean = false,
    val sent: Boolean = false,
)

/**
 * Recuperação de senha (backlog E1-10).
 *
 * A confirmação é deliberadamente ambígua — "se o e-mail estiver cadastrado" — e o resultado é
 * [PasswordRecoveryUiState.sent] mesmo quando o endereço não existe. Mensagem que distingue os dois
 * casos transforma a tela em um verificador de quem tem conta no produto.
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
            when (val result = authRepository.sendPasswordReset(current.email)) {
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

@Composable
internal fun PasswordRecoveryScreen(
    state: PasswordRecoveryUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(
            text = stringResource(R.string.auth_recovery_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(R.string.auth_recovery_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AppTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.auth_email),
            errorMessage = state.emailError?.message(),
            enabled = !state.submitting,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        )

        state.failure?.let { failure ->
            Text(
                text = failure.message(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (state.sent) {
            Text(
                text = stringResource(R.string.auth_recovery_sent),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        AppButton(
            text = stringResource(R.string.auth_recovery_action),
            onClick = onSubmit,
            loading = state.submitting,
        )

        AppTextButton(
            text = stringResource(R.string.auth_go_to_sign_in),
            onClick = onBack,
            enabled = !state.submitting,
        )
    }
}
