package com.gabrielfreire.runandlift.feature.auth.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.component.rememberLegalDocumentOpener
import com.gabrielfreire.runandlift.feature.auth.location.PickedLocationEffect
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.feature.auth.navigation.continueAfterAuth
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions

/**
 * Liga a tela de criar conta ao seu ViewModel e ao grafo.
 *
 * @param entry a entrada desta tela na pilha. Vem de fora, e não de `navController.currentBackStack
 *   Entry`, porque é nela que a tela de seleção de localidade deixa a escolha — e ler "a entrada
 *   atual" durante a animação de volta pode devolver a tela que está saindo.
 */
@Composable
internal fun SignUpDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
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

    PickedLocationEffect(
        handle = entry.savedStateHandle,
        onStatePicked = viewModel.form::onStatePicked,
        onCityPicked = viewModel.form::onCityPicked,
    )

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
            onNameChange = viewModel.form::onNameChange,
            onBirthDateChange = viewModel.form::onBirthDateChange,
            onPhoneChange = viewModel.form::onPhoneChange,
            onCrefChange = viewModel.form::onCrefChange,
            onOpenStatePicker = { navController.navigate(AuthRoutes.STATE_PICKER) },
            // A sigla já escolhida vai na rota: é ela que decide qual lista de municípios abrir.
            // O campo só é tocável depois de haver um estado, então aqui ele nunca está vazio.
            onOpenCityPicker = {
                form.stateUf.takeIf { it.isNotEmpty() }
                    ?.let { navController.navigate(AuthRoutes.cityPicker(it)) }
            },
            onTermsChange = viewModel.form::onTermsChange,
            onMarketingChange = viewModel.form::onMarketingChange,
            onOpenLegalDocument = openLegalDocument,
        ),
    )
}
