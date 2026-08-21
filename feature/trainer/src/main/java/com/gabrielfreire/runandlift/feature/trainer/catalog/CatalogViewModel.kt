package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.repository.CatalogSyncResult
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import com.gabrielfreire.runandlift.feature.trainer.catalog.CatalogFilter.Companion.toggled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O catálogo de exercícios, servido do banco local.
 *
 * **Esta é a primeira tela do produto a consumir `ExerciseRepository`**, que existe desde o E0-03 e
 * nunca foi usado. Nada nele precisou mudar: ler não toca a rede, a busca roda no SQLite e a
 * sincronização é uma chamada à parte, que só baixa se a versão publicada no Remote Config for maior
 * que a do aparelho.
 *
 * A sincronização é disparada **uma vez**, na criação, e o seu resultado não bloqueia a tela: o que
 * está em disco aparece imediatamente e é substituído quando — e se — o download terminar. Falhar é
 * estado normal e não vira erro; só há mensagem quando o disco está vazio, que é o único caso em que
 * a pessoa fica sem nada para escolher.
 *
 * O texto digitado passa por um `debounce` porque cada mudança refaz a consulta no banco; sem ele,
 * digitar "agachamento" dispara onze consultas para mostrar o resultado de uma.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class CatalogViewModel(private val exerciseRepository: ExerciseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        observeCatalog()
        sync()
    }

    /**
     * Liga o campo de busca ao banco.
     *
     * `flatMapLatest` porque uma busca nova torna a anterior irrelevante — e o `Flow` do Room fica
     * vivo enquanto ninguém o cancela, então sem isto cada letra digitada deixaria um observador de
     * tabela para trás.
     */
    private fun observeCatalog() {
        viewModelScope.launch {
            _uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MS)
                .flatMapLatest { query -> exerciseRepository.search(query) }
                .onEach { exercises ->
                    _uiState.update { it.copy(loading = false, results = exercises) }
                }
                .collect {}
        }
    }

    private fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncing = true) }

            val result = exerciseRepository.syncIfOutdated()

            _uiState.update {
                it.copy(syncing = false, syncFailed = result is CatalogSyncResult.Failed)
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onToggleCategory(category: ExerciseCategory) {
        _uiState.update { it.copy(filter = it.filter.copy(categories = it.filter.categories.toggled(category))) }
    }

    fun onToggleMuscle(muscle: String) {
        _uiState.update { it.copy(filter = it.filter.copy(muscleGroups = it.filter.muscleGroups.toggled(muscle))) }
    }

    fun onToggleEquipment(equipment: String) {
        _uiState.update { it.copy(filter = it.filter.copy(equipment = it.filter.equipment.toggled(equipment))) }
    }

    fun onToggleLevel(level: TrainingLevel) {
        _uiState.update { it.copy(filter = it.filter.copy(levels = it.filter.levels.toggled(level))) }
    }

    /** Limpa os chips e mantém o texto: são duas coisas, e quem limpa uma raramente quer a outra. */
    fun onClearFilters() {
        _uiState.update { it.copy(filter = CatalogFilter()) }
    }

    /** Tenta baixar o catálogo de novo. Só aparece na tela quando não há nada em disco. */
    fun onRetry() {
        sync()
    }

    private companion object {
        /**
         * 250 ms entre a última tecla e a consulta.
         *
         * É o intervalo em que uma pessoa digitando não percebe espera e o banco não é consultado
         * onze vezes para responder uma pergunta.
         */
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
