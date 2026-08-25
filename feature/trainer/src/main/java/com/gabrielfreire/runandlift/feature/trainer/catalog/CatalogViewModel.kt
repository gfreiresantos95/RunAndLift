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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 *
 * **Toda mudança de busca ou de filtro acende [CatalogUiState.recomputing] por uma janela mínima**,
 * mesmo quando o trabalho termina antes do quadro seguinte — que é o caso dos chips, filtrados em
 * memória. Ver [recompute].
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class CatalogViewModel(private val exerciseRepository: ExerciseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    /**
     * A janela de resposta em curso.
     *
     * Guardada para ser **cancelada**: sem isso, marcar três chips seguidos deixaria três esperas
     * correndo juntas, e a primeira a terminar apagaria o indicador enquanto as outras duas ainda
     * valiam. Cancelar e recomeçar é o que faz a janela contar a partir do último toque.
     */
    private var recomputeJob: Job? = null

    init {
        observeCatalog()
        sync()
    }

    /**
     * Liga a tela ao banco, por dois caminhos que respondem perguntas diferentes.
     *
     * O **catálogo inteiro** alimenta os chips de músculo e de equipamento. Ele é observado à parte
     * da busca de propósito: enquanto as opções saíam do resultado da busca, elas encolhiam a cada
     * letra digitada — a pessoa via as opções de filtro sumirem justamente enquanto procurava.
     *
     * A **busca** alimenta a lista. `flatMapLatest` porque uma busca nova torna a anterior
     * irrelevante — e o `Flow` do Room fica vivo enquanto ninguém o cancela, então sem isto cada
     * letra digitada deixaria um observador de tabela para trás.
     *
     * `loading` é desligado pela busca, e não pelo catálogo: as duas emitem quase juntas, e desligar
     * na primeira que chegar deixaria uma fração de segundo com o vocabulário dos chips já montado e
     * a lista ainda vazia — que a tela leria como "a busca não achou nada".
     */
    private fun observeCatalog() {
        viewModelScope.launch {
            exerciseRepository.observeAll().collect { exercises ->
                _uiState.update { it.copy(catalog = exercises) }
            }
        }

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

    /**
     * Baixa o catálogo, se a versão publicada for maior que a do aparelho.
     *
     * **Pública, e é ela mesma que o botão de "tentar de novo" chama.** Havia um `onRetry()` que não
     * fazia nada além de chamar esta — e um método que só encaminha é um nome a mais para a mesma
     * coisa, com a chance de os dois divergirem no dia em que um deles ganhar uma linha. A tela
     * continua chamando a ação de "tentar de novo", que é o que ela significa lá.
     */
    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(syncing = true) }

            val result = exerciseRepository.syncIfOutdated()

            _uiState.update {
                it.copy(syncing = false, syncFailed = result is CatalogSyncResult.Failed)
            }
        }
    }

    /**
     * Muda o que a lista mostra e acende o indicador por uma janela mínima.
     *
     * **Passa por aqui tudo o que refaz a lista** — a busca e os quatro filtros. É uma função só
     * porque a espera não é do trabalho, é da **resposta**: marcar um chip filtra em memória e
     * termina antes do quadro seguinte, e sem esta janela a lista apenas encolhia. Encolher sem
     * aviso se lê como defeito; um instante de indicador se lê como o app tendo entendido o toque.
     *
     * A janela anterior é cancelada, e não somada: marcar três chips seguidos deixaria três esperas
     * correndo juntas, e a primeira a terminar apagaria o indicador com as outras duas ainda
     * valendo. Cancelando, a contagem recomeça do último toque — que é o que a pessoa acabou de
     * fazer.
     *
     * @param change o que muda no estado. Aplicado **junto** de acender o indicador, numa emissão
     *   só: em duas, existiria um quadro com a lista nova e o indicador ainda apagado, que é
     *   exatamente o piscar que isto veio remover.
     */
    private fun recompute(change: (CatalogUiState) -> CatalogUiState) {
        recomputeJob?.cancel()
        _uiState.update { change(it).copy(recomputing = true) }

        recomputeJob = viewModelScope.launch {
            delay(timeMillis = RECOMPUTE_FEEDBACK_MS)
            _uiState.update { it.copy(recomputing = false) }
        }
    }

    fun onQueryChange(query: String) {
        recompute { it.copy(query = query) }
    }

    fun onToggleCategory(category: ExerciseCategory) {
        recompute { it.copy(filter = it.filter.copy(categories = it.filter.categories.toggled(category))) }
    }

    fun onToggleMuscle(muscle: String) {
        recompute { it.copy(filter = it.filter.copy(muscleGroups = it.filter.muscleGroups.toggled(muscle))) }
    }

    fun onToggleEquipment(equipment: String) {
        recompute { it.copy(filter = it.filter.copy(equipment = it.filter.equipment.toggled(equipment))) }
    }

    fun onToggleLevel(level: TrainingLevel) {
        recompute { it.copy(filter = it.filter.copy(levels = it.filter.levels.toggled(level))) }
    }

    /**
     * Abre ou fecha as fileiras de chip.
     *
     * Mora no estado, e não num `remember` da tela, porque é a única forma de o preview mostrar as
     * duas situações — e porque estado de tela que só existe dentro da composição é estado que se
     * perde quando o processo é recriado.
     */
    fun onToggleFilters() {
        _uiState.update { it.copy(filtersExpanded = !it.filtersExpanded) }
    }

    /** Limpa os chips e mantém o texto: são duas coisas, e quem limpa uma raramente quer a outra. */
    fun onClearFilters() {
        recompute { it.copy(filter = CatalogFilter()) }
    }

    private companion object {
        /**
         * 250 ms entre a última tecla e a consulta.
         *
         * É o intervalo em que uma pessoa digitando não percebe espera e o banco não é consultado
         * onze vezes para responder uma pergunta.
         */
        const val SEARCH_DEBOUNCE_MS = 250L

        /**
         * 300 ms de indicador a cada mudança de busca ou de filtro.
         *
         * É a janela mínima de **resposta**, não o tempo do trabalho: os chips filtram em memória e
         * a busca sai do SQLite, então na prática nada demora isso. O número foi escolhido por dois
         * lados. Abaixo de uns 200 ms o indicador entra e sai dentro da mesma piscada e ninguém
         * registra que ele existiu — que é o mesmo que não ter mostrado nada. Acima de uns 400 ms a
         * espera passa a ser sentida como lentidão do app, e aí o remédio vira o sintoma.
         *
         * Também cobre o [SEARCH_DEBOUNCE_MS] com folga, e é de propósito: enquanto a consulta
         * espera as teclas pararem, a lista na tela é a da busca anterior — e o indicador é o que
         * diz isso em vez de deixar um resultado velho passando por atual.
         */
        const val RECOMPUTE_FEEDBACK_MS = 300L
    }
}
