package com.gabrielfreire.runandlift.feature.auth.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class RoleSelectionUiState(
    val selected: ActiveRole? = null,
    val submitting: Boolean = false,
    val failed: Boolean = false,
    val confirmedRole: ActiveRole? = null,
)

/**
 * Escolha de papel **depois** de autenticar (backlog E1-02).
 *
 * Desde que existe [WelcomeScreen], esta tela é a rede de segurança, não o caminho comum: quem
 * passa pelas boas-vindas chega ao papel já gravado pelo cadastro. Ela ainda é alcançada por conta
 * que existe sem papel — sessão anterior à escolha, primeiro login com Google feito pela tela de
 * entrar, ou gravação que falhou no cadastro.
 *
 * Grava o papel em `users/{uid}` **somando** ao que já existir, nunca substituindo: é o que
 * permite a mesma conta ser treinador e aluno de outro treinador, sem segundo login (§3.2).
 */
internal class RoleSelectionViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleSelectionUiState())
    val uiState: StateFlow<RoleSelectionUiState> = _uiState.asStateFlow()

    fun onSelect(role: ActiveRole) {
        _uiState.update { it.copy(selected = role, failed = false) }
    }

    fun onConfirm() {
        val current = _uiState.value
        val role = current.selected
        val account = authRepository.currentAccountOrNull()

        // Sem papel escolhido ou já enviando: nada a fazer, e nada a sinalizar.
        if (role == null || current.submitting) return

        // Sem conta é falha de verdade — significa que a sessão caiu entre a tela anterior e esta.
        if (account == null) {
            _uiState.update { it.copy(failed = true) }
            return
        }

        _uiState.update { it.copy(submitting = true, failed = false) }

        viewModelScope.launch {
            runCatching {
                userRepository.saveProfile(
                    uid = account.uid,
                    role = role,
                    // Nome derivado do e-mail só entra se ainda não houver um: quem passou pelo
                    // formulário de cadastro já informou o nome real, e o repositório preserva.
                    details = SignUpDetails(displayName = account.email?.substringBefore('@')),
                )
            }.onSuccess { profile ->
                _uiState.update { it.copy(submitting = false, confirmedRole = profile.activeRole) }
            }.onFailure {
                _uiState.update { it.copy(submitting = false, failed = true) }
            }
        }
    }
}

@Composable
internal fun RoleSelectionScreen(
    state: RoleSelectionUiState,
    onSelect: (ActiveRole) -> Unit,
    onConfirm: () -> Unit,
    onConfirmed: (ActiveRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.confirmedRole) {
        state.confirmedRole?.let(onConfirmed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = stringResource(R.string.onboarding_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
        ) {
            RoleOptionCard(
                title = stringResource(R.string.onboarding_student),
                description = stringResource(R.string.onboarding_student_description),
                selected = state.selected == ActiveRole.STUDENT,
                onClick = { onSelect(ActiveRole.STUDENT) },
                enabled = !state.submitting,
            )
            RoleOptionCard(
                title = stringResource(R.string.onboarding_trainer),
                description = stringResource(R.string.onboarding_trainer_description),
                selected = state.selected == ActiveRole.TRAINER,
                onClick = { onSelect(ActiveRole.TRAINER) },
                enabled = !state.submitting,
            )
        }

        if (state.failed) {
            Text(
                text = stringResource(R.string.auth_error_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Start,
            )
        }

        AppButton(
            text = stringResource(R.string.onboarding_confirm),
            onClick = onConfirm,
            enabled = state.selected != null,
            loading = state.submitting,
        )
    }
}

/**
 * O estado em que a tela abre — nada escolhido, botão desabilitado. É o que se confere aqui:
 * "Continuar" só liga depois de existir uma escolha, porque confirmar o vazio não é uma ação.
 */
@Preview(name = "Escolha de papel · claro", showBackground = true, heightDp = 600)
@Preview(
    name = "Escolha de papel · escuro",
    showBackground = true,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RoleSelectionPreview() {
    RunAndLiftTheme {
        RoleSelectionScreen(
            state = RoleSelectionUiState(),
            onSelect = {},
            onConfirm = {},
            onConfirmed = {},
        )
    }
}

/** Papel escolhido e gravação que falhou: o erro fica em texto, acima do botão, e não some. */
@Preview(name = "Escolha de papel · falha", showBackground = true, heightDp = 600)
@Composable
private fun RoleSelectionFailurePreview() {
    RunAndLiftTheme {
        RoleSelectionScreen(
            state = RoleSelectionUiState(selected = ActiveRole.TRAINER, failed = true),
            onSelect = {},
            onConfirm = {},
            onConfirmed = {},
        )
    }
}
