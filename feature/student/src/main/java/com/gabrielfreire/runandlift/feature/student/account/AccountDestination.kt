package com.gabrielfreire.runandlift.feature.student.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies

/** Liga a tela de dados cadastrais ao seu ViewModel. */
@Composable
internal fun AccountDestination(
    dependencies: StudentDependencies,
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AccountViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
    )
}
