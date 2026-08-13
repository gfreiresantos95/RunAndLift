package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies

/** Liga a escolha de papel ao seu ViewModel. Não navega: quem decide para onde ir é `:app`. */
@Composable
internal fun RoleSelectionDestination(
    dependencies: AuthDependencies,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: RoleSelectionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RoleSelectionViewModel(dependencies.authRepository, dependencies.userRepository)
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RoleSelectionScreen(
        state = state,
        onSelect = viewModel::onSelect,
        onConfirm = viewModel::onConfirm,
        onConfirmed = onAuthenticatedWithRole,
    )
}
