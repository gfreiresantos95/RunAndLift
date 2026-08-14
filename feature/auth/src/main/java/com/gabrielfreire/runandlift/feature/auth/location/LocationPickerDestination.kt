package com.gabrielfreire.runandlift.feature.auth.location

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
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies

/**
 * Liga a tela de seleção de localidade ao seu ViewModel e devolve a escolha a quem a abriu.
 *
 * Um destino para as duas telas, distinguidas por [uf]: `null` lista estados, preenchido lista os
 * municípios daquele estado. O que difere de verdade são os textos, e é por isso que eles são a
 * única coisa ramificada aqui.
 *
 * **A escolha volta pelo `SavedStateHandle` da entrada anterior**, e não por um callback. Callback
 * exigiria que o formulário continuasse composto enquanto esta tela está por cima, e ele não
 * continua — o Compose Navigation o tira da composição. O `SavedStateHandle` sobrevive à travessia
 * e, de quebra, a uma morte de processo no meio dela.
 *
 * @param uf estado dos municípios a listar, ou `null` para a lista de estados.
 */
@Composable
internal fun LocationPickerDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
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
        if (listingCities) R.string.auth_city_picker_title else R.string.auth_state_picker_title,
    ),
    searchLabel = stringResource(
        if (listingCities) R.string.auth_city_picker_search else R.string.auth_state_picker_search,
    ),
    clearSearch = stringResource(R.string.auth_picker_clear),
    empty = stringResource(
        if (listingCities) R.string.auth_city_picker_empty else R.string.auth_state_picker_empty,
    ),
    failure = stringResource(R.string.auth_picker_failure),
    retry = stringResource(R.string.auth_picker_retry),
    back = stringResource(R.string.auth_back),
)

/**
 * Escreve a escolha na entrada anterior e se desempilha.
 *
 * A ordem importa: escrever **antes** de desempilhar. Depois do `popBackStack` esta entrada já saiu,
 * e `previousBackStackEntry` passaria a apontar para outra coisa — ou para nada.
 *
 * @param state preenchido quando o que se escolheu foi um estado. Nesse caso vão as duas metades,
 *   porque o formulário precisa da sigla para gravar e do nome para desenhar.
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
