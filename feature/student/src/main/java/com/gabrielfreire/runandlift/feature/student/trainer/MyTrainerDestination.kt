package com.gabrielfreire.runandlift.feature.student.trainer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies

/** Liga a tela do treinador do aluno ao seu ViewModel. */
@Composable
internal fun MyTrainerDestination(
    dependencies: StudentDependencies,
    onBack: () -> Unit,
    viewModel: MyTrainerViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                MyTrainerViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    linkRepository = dependencies.linkRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyTrainerScreen(
        state = state,
        actions = MyTrainerActions(
            onCodeChange = viewModel::onCodeChange,
            onSubmitCode = viewModel::onSubmitCode,
            onConfirmInvite = viewModel::onConfirmInvite,
            onDismissInvite = viewModel::onDismissInvite,
            onStatusChange = viewModel::onStatusChange,
            onBack = onBack,
        ),
    )
}
