package com.gabrielfreire.runandlift.feature.auth.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.core.designsystem.component.AppPickerState
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.location.LocationSearch
import com.gabrielfreire.runandlift.data.model.BrazilState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A lista de uma tela de seleção de localidade — estados, ou os municípios de um estado.
 *
 * **Um ViewModel para as duas telas**, e não um por lista: o que muda entre elas é de onde os textos
 * vêm, e isso é uma linha. Tudo o mais — carregar, filtrar, falhar, tentar de novo — é idêntico, e
 * duas classes o descreveriam duas vezes.
 *
 * O que a lista mostra e o que ela devolve **não são a mesma string** no caso do estado: mostra
 * `São Paulo - SP` e devolve `SP`. Por isso os estados carregados ficam guardados em [states] — a
 * tela de seleção do `:core` trabalha com texto puro, e é aqui que o texto escolhido volta a ser o
 * par sigla-e-nome que o formulário precisa.
 *
 * A busca é aplicada **sobre a lista já carregada**, em memória: os 853 municípios do maior estado
 * cabem folgados, e um filtro que fosse à rede a cada tecla digitada seria uma requisição por letra.
 *
 * @param uf estado cujos municípios listar, ou `null` para listar os estados. É o único parâmetro
 *   que distingue as duas telas.
 */
internal class LocationPickerViewModel(
    private val locationRepository: LocationRepository,
    private val uf: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppPickerState>(AppPickerState.Loading)
    val uiState: StateFlow<AppPickerState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** O que o IBGE devolveu, sem filtro. Vazio até carregar, e é dele que sai tudo o que a tela vê. */
    private var loaded: List<String> = emptyList()

    /** Os estados como objetos, para traduzir de volta o texto escolhido. Vazio na tela de cidades. */
    private var states: List<BrazilState> = emptyList()

    init {
        load()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        _uiState.update { current ->
            // Só refiltra quando há lista: filtrar durante o carregamento ou depois de uma falha
            // trocaria o indicador — ou o aviso — por uma lista vazia, que diz outra coisa.
            if (current is AppPickerState.Options) AppPickerState.Options(filtered(value)) else current
        }
    }

    /** Nova tentativa depois de uma falha. Recomeça do zero, inclusive a busca já digitada. */
    fun onRetry() {
        _uiState.value = AppPickerState.Loading
        load()
    }

    /**
     * O estado por trás de um item escolhido, ou `null` na tela de cidades.
     *
     * Existe porque a tela devolve o texto que exibiu, e no caso do estado o texto é `São Paulo -
     * SP` enquanto o banco quer `SP`. Refazer a separação por string no destino seria conhecer o
     * formato de [BrazilState.label] num segundo lugar.
     */
    fun stateOf(label: String): BrazilState? = states.firstOrNull { it.label == label }

    private fun load() {
        viewModelScope.launch {
            val result = runCatching {
                if (uf == null) {
                    locationRepository.states().also { states = it }.map(BrazilState::label)
                } else {
                    locationRepository.cities(uf)
                }
            }

            loaded = result.getOrDefault(emptyList())
            _uiState.value = result
                .map { AppPickerState.Options(filtered(_query.value)) }
                .getOrElse { AppPickerState.Failed }
        }
    }

    private fun filtered(query: String): List<String> =
        loaded.filter { LocationSearch.matches(candidate = it, query = query) }
}
