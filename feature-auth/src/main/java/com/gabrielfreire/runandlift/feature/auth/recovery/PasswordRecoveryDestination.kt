package com.gabrielfreire.runandlift.feature.auth.recovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.data.auth.AuthRepository

/**
 * Liga a recuperação de senha ao seu ViewModel.
 *
 * Recebe o repositório solto, e não [com.gabrielfreire.runandlift.feature.auth.navigation
 * .AuthDependencies] inteiro: esta é a única tela do fluxo que não lê perfil nem abre a folha do
 * Google, e pedir as três dependências esconderia isso.
 */
@Composable
internal fun PasswordRecoveryDestination(
    navController: NavHostController,
    authRepository: AuthRepository,
    viewModel: PasswordRecoveryViewModel = viewModel(
        factory = viewModelFactory { initializer { PasswordRecoveryViewModel(authRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PasswordRecoveryScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::onSubmit,
        onBack = { navController.popBackStack() },
    )
}
