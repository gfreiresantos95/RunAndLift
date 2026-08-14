package com.gabrielfreire.runandlift.feature.student.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerActions
import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerTexts
import com.gabrielfreire.runandlift.core.designsystem.component.AppSearchablePicker
import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies

/**
 * Liga a tela de seleção de localidade ao seu ViewModel e devolve a escolha a quem a abriu.
 *
 * Um destino para as duas telas, distinguidas por [uf]: `null` lista estados, preenchido lista os
 * municípios daquele estado.
 *
 * **A escolha volta pelo `SavedStateHandle` da entrada anterior**, e não por um callback: a tela que
 * a abriu sai da composição enquanto esta está por cima, e um callback morreria com ela.
 *
 * @param uf estado dos municípios a listar, ou `null` para a lista de estados.
 */
@Composable
internal fun LocationPickerDestination(
    navController: NavHostController,
    dependencies: StudentDependencies,
    uf: String?,
    viewModel: LocationPickerViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LocationPickerViewModel(dependencies.locationRepository, uf) }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    AppSearchablePicker(
        texts = pickerTexts(listingCities = uf != null),
        state = state,
        query = query,
        actions = AppPickerActions(
            onQueryChange = viewModel::onQueryChange,
            onSelect = { label -> navController.deliver(label, viewModel.stateOf(label)) },
            onRetry = viewModel::onRetry,
            onBack = { navController.popBackStack() },
        ),
    )
}

/**
 * Os textos de cada uma das duas listas.
 *
 * Título, rótulo de busca e a frase de "não achei" mudam; limpar, falha, nova tentativa e voltar
 * não mudam — são sobre o mecanismo da tela, e não sobre o que ela lista.
 */
@Composable
private fun pickerTexts(listingCities: Boolean) = AppPickerTexts(
    title = stringResource(
        if (listingCities) R.string.student_city_picker_title else R.string.student_state_picker_title,
    ),
    searchLabel = stringResource(
        if (listingCities) R.string.student_city_picker_search else R.string.student_state_picker_search,
    ),
    clearSearch = stringResource(R.string.student_picker_clear),
    empty = stringResource(
        if (listingCities) R.string.student_city_picker_empty else R.string.student_state_picker_empty,
    ),
    failure = stringResource(R.string.student_picker_failure),
    retry = stringResource(R.string.student_picker_retry),
    back = stringResource(R.string.student_action_back),
)

/**
 * Escreve a escolha na entrada anterior e se desempilha.
 *
 * A ordem importa: escrever **antes** de desempilhar. Depois do `popBackStack` esta entrada já saiu,
 * e `previousBackStackEntry` passaria a apontar para outra coisa — ou para nada.
 */
private fun NavHostController.deliver(label: String, state: BrazilState?) {
    val handle = previousBackStackEntry?.savedStateHandle

    if (state == null) {
        handle?.set(LocationPickerResult.CITY, label)
    } else {
        handle?.set(LocationPickerResult.STATE_UF, state.uf)
        handle?.set(LocationPickerResult.STATE_NAME, state.name)
    }

    popBackStack()
}
