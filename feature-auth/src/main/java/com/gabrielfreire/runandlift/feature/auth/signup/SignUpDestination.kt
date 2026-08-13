package com.gabrielfreire.runandlift.feature.auth.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.component.rememberLegalDocumentOpener
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies
import com.gabrielfreire.runandlift.feature.auth.navigation.continueAfterAuth
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions

/** Liga a tela de criar conta ao seu ViewModel e ao grafo. */
@Composable
internal fun SignUpDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
    intendedRole: ActiveRole?,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: SignUpViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SignUpViewModel(dependencies.authRepository, dependencies.userRepository, intendedRole)
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val openLegalDocument = rememberLegalDocumentOpener()

    SignUpScreen(
        state = state,
        form = form,
        role = intendedRole,
        actions = SignUpActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // Desempilha, não navega: só se chega ao cadastro **pela** entrada, então a entrada
            // está logo abaixo, com o formulário que a pessoa já preencheu. Navegar empilharia uma
            // segunda cópia dela e faria "voltar" atravessar duas telas iguais.
            onSignIn = { navController.popBackStack() },
            onAuthenticated = { navController.continueAfterAuth(state, onAuthenticatedWithRole) },
            onBack = { navController.popBackStack() },
        ),
        formActions = ProfileFormActions(
            onNameChange = viewModel::onNameChange,
            onBirthDateChange = viewModel::onBirthDateChange,
            onPhoneChange = viewModel::onPhoneChange,
            onCrefChange = viewModel::onCrefChange,
            onTermsChange = viewModel::onTermsChange,
            onMarketingChange = viewModel::onMarketingChange,
            onOpenLegalDocument = openLegalDocument,
        ),
    )
}
