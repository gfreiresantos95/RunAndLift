package com.gabrielfreire.runandlift.feature.student.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.location.PickedLocationEffect
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes

/**
 * Liga a tela de dados cadastrais ao seu ViewModel.
 *
 * @param entry a entrada desta tela na pilha. Vem de fora, e não de `navController.currentBackStack
 *   Entry`, porque é nela que a tela de seleção de localidade deixa a escolha — e ler "a entrada
 *   atual" durante a animação de volta pode devolver a tela que está saindo.
 */
@Composable
internal fun AccountDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: StudentDependencies,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AccountViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    locationRepository = dependencies.locationRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PickedLocationEffect(
        handle = entry.savedStateHandle,
        onStatePicked = viewModel::onStatePicked,
        onCityPicked = viewModel::onCityPicked,
    )

    AccountScreen(
        state = state,
        actions = AccountActions(
            onNameChange = viewModel::onNameChange,
            onPhoneChange = viewModel::onPhoneChange,
            onOpenStatePicker = { navController.navigate(StudentRoutes.STATE_PICKER) },
            // A sigla já escolhida vai na rota: é ela que decide qual lista de municípios abrir.
            // O campo só é tocável depois de haver um estado, então aqui ele nunca está vazio.
            onOpenCityPicker = {
                state.stateUf.takeIf { it.isNotEmpty() }
                    ?.let { navController.navigate(StudentRoutes.cityPicker(it)) }
            },
            onSubmit = viewModel::onSubmit,
            onSaved = onSaved,
            onBack = onBack,
        ),
    )
}
