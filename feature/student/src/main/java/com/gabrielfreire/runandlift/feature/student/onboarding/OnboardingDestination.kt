package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions

/**
 * Liga o onboarding ao seu ViewModel.
 *
 * [onFinished] é chamado quando o fluxo termina — com respostas, pulado inteiro, ou depois de uma
 * gravação que falhou. **Terminar não é sinônimo de ter respondido**, e é por isso que a home tem
 * o aviso: o que ficou de fora é cobrado lá, não aqui.
 */
@Composable
internal fun OnboardingDestination(
    dependencies: StudentDependencies,
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                OnboardingViewModel(
                    authRepository = dependencies.authRepository,
                    studentRepository = dependencies.studentRepository,
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
        actions = TrainingFormActions(
            onLevelSelect = viewModel::onLevelSelect,
            onGoalSelect = viewModel::onGoalSelect,
            onDayToggle = viewModel::onDayToggle,
            onWeightChange = viewModel::onWeightChange,
            onHeightChange = viewModel::onHeightChange,
            onRestrictionsChange = viewModel::onRestrictionsChange,
            onHealthConsentChange = viewModel::onHealthConsentChange,
        ),
        onNext = { viewModel.onStepDone(answered = true) },
        onSkip = { viewModel.onStepDone(answered = false) },
    )
}
