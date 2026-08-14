package com.gabrielfreire.runandlift.feature.student.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions

/** Liga a edição de perfil ao seu ViewModel. */
@Composable
internal fun StudentProfileDestination(
    dependencies: StudentDependencies,
    onBack: () -> Unit,
    viewModel: StudentProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                StudentProfileViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    studentRepository = dependencies.studentRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()

    StudentProfileScreen(
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
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
    )
}
