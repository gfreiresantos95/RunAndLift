package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/**
 * Liga o passo a passo do treinador ao seu ViewModel.
 *
 * [onFinished] é chamado quando o fluxo termina — com respostas, pulado inteiro, ou depois de uma
 * gravação que falhou. **Terminar não é sinônimo de ter respondido**, e é por isso que a home tem
 * o aviso: o que ficou de fora é cobrado lá, não aqui.
 */
@Composable
internal fun OnboardingDestination(
    dependencies: TrainerDependencies,
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                OnboardingViewModel(
                    authRepository = dependencies.authRepository,
                    trainerRepository = dependencies.trainerRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    OnboardingScreen(
        state = state,
        form = form,
        actions = viewModel.formActions,
        steps = OnboardingStepActions(
            onNext = { viewModel.onStepDone(answered = true) },
            onSkip = { viewModel.onStepDone(answered = false) },
            onBack = viewModel::onBack,
        ),
    )
}
