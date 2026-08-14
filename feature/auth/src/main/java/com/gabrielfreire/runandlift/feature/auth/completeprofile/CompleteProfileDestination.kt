package com.gabrielfreire.runandlift.feature.auth.completeprofile

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
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions

/**
 * Conclusão de cadastro: pede o que o provedor de entrada não tinha para dar, e grava o papel.
 *
 * @param entry a entrada desta tela na pilha, onde a seleção de localidade deixa a escolha. Mesma
 *   mecânica do cadastro por formulário — ver `SignUpDestination`.
 */
@Composable
internal fun CompleteProfileDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: AuthDependencies,
    role: ActiveRole,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: CompleteProfileViewModel = viewModel(
        // O papel na chave: trocar de papel na mesma rota precisa recomeçar com outra régua, e um
        // ViewModel reaproveitado traria o "o que falta" do papel anterior.
        key = role.storageValue,
        factory = viewModelFactory {
            initializer {
                CompleteProfileViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    locationRepository = dependencies.locationRepository,
                    role = role,
                )
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

    CompleteProfileScreen(
        state = state,
        form = form,
        onSubmit = viewModel::onSubmit,
        onCompleted = onAuthenticatedWithRole,
        actions = ProfileFormActions(
            // O nome veio do provedor e não tem campo nesta tela; a ação existe só porque os
            // blocos de campo são os mesmos do cadastro, e reaproveitá-los vale mais do que
            // duplicar contrato para omitir uma lambda.
            onNameChange = {},
            onBirthDateChange = viewModel.form::onBirthDateChange,
            onPhoneChange = viewModel.form::onPhoneChange,
            onCrefChange = viewModel.form::onCrefChange,
            onOpenStatePicker = { navController.navigate(AuthRoutes.STATE_PICKER) },
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
