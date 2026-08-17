package com.gabrielfreire.runandlift.feature.trainer.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/** Liga a edição do perfil profissional ao seu ViewModel. */
@Composable
internal fun TrainerProfileDestination(
    dependencies: TrainerDependencies,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: TrainerProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                TrainerProfileViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    trainerRepository = dependencies.trainerRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    TrainerProfileScreen(
        state = state,
        form = form,
        formActions = viewModel.formActions,
        actions = TrainerProfileActions(
            onSubmit = viewModel::onSubmit,
            onSaved = onSaved,
            onBack = onBack,
        ),
    )
}
