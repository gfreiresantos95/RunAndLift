package com.gabrielfreire.runandlift.feature.auth.completeprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.component.rememberLegalDocumentOpener
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions

/** Conclusão de cadastro: pede o que o provedor de entrada não tinha para dar, e grava o papel. */
@Composable
internal fun CompleteProfileDestination(
    dependencies: AuthDependencies,
    role: ActiveRole,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: CompleteProfileViewModel = viewModel(
        // O papel na chave: trocar de papel na mesma rota precisa recomeçar com outra régua, e um
        // ViewModel reaproveitado traria o "o que falta" do papel anterior.
        key = role.storageValue,
        factory = viewModelFactory {
            initializer {
                CompleteProfileViewModel(dependencies.authRepository, dependencies.userRepository, role)
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val openLegalDocument = rememberLegalDocumentOpener()

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
            onBirthDateChange = viewModel::onBirthDateChange,
            onPhoneChange = viewModel::onPhoneChange,
            onCrefChange = viewModel::onCrefChange,
            onTermsChange = viewModel::onTermsChange,
            onMarketingChange = viewModel::onMarketingChange,
            onOpenLegalDocument = openLegalDocument,
        ),
    )
}
