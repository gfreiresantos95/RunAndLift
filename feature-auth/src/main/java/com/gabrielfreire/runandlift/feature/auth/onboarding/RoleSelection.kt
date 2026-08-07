package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
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
 * Bifurcação "sou aluno" / "sou treinador" (backlog E1-02).
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
                userRepository.addRole(
                    uid = account.uid,
                    role = role,
                    displayName = account.email?.substringBefore('@'),
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
            RoleCard(
                title = stringResource(R.string.onboarding_student),
                description = stringResource(R.string.onboarding_student_description),
                selected = state.selected == ActiveRole.STUDENT,
                enabled = !state.submitting,
                onClick = { onSelect(ActiveRole.STUDENT) },
            )
            RoleCard(
                title = stringResource(R.string.onboarding_trainer),
                description = stringResource(R.string.onboarding_trainer_description),
                selected = state.selected == ActiveRole.TRAINER,
                enabled = !state.submitting,
                onClick = { onSelect(ActiveRole.TRAINER) },
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

@Composable
private fun RoleCard(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        // `selectable` com Role.RadioButton, e não `clickable`: é o que faz o leitor de tela
        // anunciar o cartão como opção escolhível e informar qual está marcada.
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
