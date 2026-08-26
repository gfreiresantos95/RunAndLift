package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.repository.CatalogSyncResult
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.exercise
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O que o preview do catálogo não mostra: a busca ligada ao banco, e o que a sincronização faz — ou
 * não faz — com a tela.
 *
 * A regra que mais importa é a de que **falhar a sincronização não é erro de tela**. O que está em
 * disco continua ali e continua servindo; só há mensagem quando o disco está vazio, que é o único
 * caso em que a pessoa fica sem nada para escolher. Trocar isso por um erro genérico esconderia o
 * catálogo inteiro de quem está sem sinal na academia — que é exatamente quando ele é necessário.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalogo = listOf(
        exercise("supino", name = "Supino reto", muscle = "Peitoral", equipment = "Barra"),
        exercise("remada", name = "Remada curvada", muscle = "Dorsal", equipment = "Halter"),
        exercise("prancha", name = "Prancha", muscle = "Abdômen", equipment = null, level = TrainingLevel.ADVANCED),
    )

    @Test
    fun `o que esta em disco aparece sem passar pela rede`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))

        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.exercises.size)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `comeca carregando antes de o banco responder`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `a busca filtra o que o banco devolve`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onQueryChange("remada")
        advanceUntilIdle()

        assertEquals(listOf("remada"), viewModel.uiState.value.exercises.map { it.id })
    }

    @Test
    fun `busca sem resultado e tela diferente de catalogo ausente`() = runTest {
        // Confundir as duas manda o treinador apagar a busca para tentar de novo, e continuar sem
        // nada: aqui não há o que refinar, o que falta é sincronizar.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onQueryChange("levantamento terra")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmptySearch)
        assertFalse(viewModel.uiState.value.isCatalogMissing)
    }

    @Test
    fun `disco vazio e catalogo ausente, e nao busca sem resultado`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository())

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCatalogMissing)
    }

    @Test
    fun `a sincronizacao que falha nao esconde o que ja estava em disco`() = runTest {
        val repository = FakeExerciseRepository(
            exercises = catalogo,
            syncResult = CatalogSyncResult.Failed(IllegalStateException("sem rede")),
        )
        val viewModel = CatalogViewModel(repository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.syncFailed)
        assertFalse(viewModel.uiState.value.syncing)
        assertEquals(3, viewModel.uiState.value.exercises.size)
    }

    @Test
    fun `tentar de novo dispara uma segunda sincronizacao`() = runTest {
        val repository = FakeExerciseRepository(
            syncResult = CatalogSyncResult.Failed(IllegalStateException("sem rede")),
        )
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        repository.syncResult = CatalogSyncResult.Updated(version = 2, exerciseCount = 3)
        viewModel.sync()
        advanceUntilIdle()

        assertEquals(2, repository.syncCount)
        assertFalse(viewModel.uiState.value.syncFailed)
    }

    @Test
    fun `o catalogo que chega depois substitui a tela vazia`() = runTest {
        // É o que a sincronização faz de verdade: a tela abre com o que há em disco e é substituída
        // quando — e se — o download terminar.
        val repository = FakeExerciseRepository()
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()

        repository.publish(catalogo)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.exercises.size)
        assertFalse(viewModel.uiState.value.isCatalogMissing)
    }

    @Test
    fun `cada chip filtra o que ja veio do banco`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleMuscle("Dorsal")

        assertEquals(listOf("remada"), viewModel.uiState.value.exercises.map { it.id })

        viewModel.onToggleMuscle("Dorsal")
        viewModel.onToggleEquipment("Barra")

        assertEquals(listOf("supino"), viewModel.uiState.value.exercises.map { it.id })
    }

    @Test
    fun `nivel e categoria tambem entram no filtro`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleLevel(TrainingLevel.ADVANCED)

        assertEquals(listOf("prancha"), viewModel.uiState.value.exercises.map { it.id })

        viewModel.onToggleLevel(TrainingLevel.ADVANCED)
        viewModel.onToggleCategory(ExerciseCategory.CARDIO)

        assertTrue(viewModel.uiState.value.exercises.isEmpty())
    }

    @Test
    fun `limpar os chips mantem o texto digitado`() = runTest {
        // São duas coisas, e quem limpa uma raramente quer a outra.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onQueryChange("prancha")
        viewModel.onToggleMuscle("Dorsal")
        advanceUntilIdle()

        viewModel.onClearFilters()

        assertEquals("prancha", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.filter.isActive)
    }

    @Test
    fun `os chips saem do proprio catalogo, e nao de uma lista fixa`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))

        advanceUntilIdle()

        assertEquals(listOf("Abdômen", "Dorsal", "Peitoral"), viewModel.uiState.value.muscleOptions)
        assertEquals(listOf("Barra", "Halter"), viewModel.uiState.value.equipmentOptions)
    }

    @Test
    fun `os chips nao encolhem quando a busca fecha a lista`() = runTest {
        // Enquanto as opções saíam do resultado da busca, procurar "prancha" apagava os chips de
        // "Dorsal" e "Peitoral" — a pessoa via as opções de filtro sumirem enquanto procurava.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onQueryChange("prancha")
        advanceUntilIdle()

        assertEquals(listOf("prancha"), viewModel.uiState.value.exercises.map { it.id })
        assertEquals(listOf("Abdômen", "Dorsal", "Peitoral"), viewModel.uiState.value.muscleOptions)
    }

    @Test
    fun `as fileiras de filtro nascem fechadas e abrem no toque`() = runTest {
        // Fechadas por padrão porque quatro fileiras de chip empurravam a lista — que é o que a
        // pessoa veio ver — para fora da primeira tela.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.filtersExpanded)

        viewModel.onToggleFilters()

        assertTrue(viewModel.uiState.value.filtersExpanded)

        viewModel.onToggleFilters()

        assertFalse(viewModel.uiState.value.filtersExpanded)
    }

    @Test
    fun `marcar um chip acende o indicador e o segura por uma janela minima`() = runTest {
        // O filtro roda em memória e termina antes do quadro seguinte. Sem a janela, a lista apenas
        // encolhia — e encolher sem aviso se lê como defeito, não como resposta ao toque.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleMuscle("Dorsal")

        assertTrue("o indicador tem de estar aceso no mesmo instante do toque", viewModel.uiState.value.recomputing)
        assertEquals(
            "a lista já é a filtrada: o que se segura é a resposta, não o trabalho",
            listOf("remada"),
            viewModel.uiState.value.exercises.map { it.id },
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.recomputing)
    }

    @Test
    fun `chips seguidos reiniciam a janela em vez de somarem tres esperas`() = runTest {
        // Sem cancelar a anterior, a primeira a terminar apagaria o indicador com as outras duas
        // ainda valendo — e ele piscaria no meio de quem está marcando três chips seguidos.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleMuscle("Dorsal")
        advanceTimeBy(delayTimeMillis = HALF_WINDOW_MS)
        viewModel.onToggleEquipment("Barra")
        advanceTimeBy(delayTimeMillis = HALF_WINDOW_MS)

        assertTrue(
            "a janela conta a partir do último toque, e o último foi agora há pouco",
            viewModel.uiState.value.recomputing,
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.recomputing)
    }

    @Test
    fun `digitar tambem acende o indicador, porque a lista na tela e a da busca anterior`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onQueryChange("remada")

        assertTrue(viewModel.uiState.value.recomputing)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.recomputing)
        assertEquals(listOf("remada"), viewModel.uiState.value.exercises.map { it.id })
    }

    @Test
    fun `limpar os filtros tambem responde, e nao so devolve a lista inteira`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleMuscle("Dorsal")
        advanceUntilIdle()

        viewModel.onClearFilters()

        assertTrue(viewModel.uiState.value.recomputing)
    }

    @Test
    fun `abrir e fechar as fileiras nao acende indicador nenhum`() = runTest {
        // Abrir o filtro não refaz a lista. Um indicador aqui seria o app dizendo que está
        // trabalhando quando não está — e é assim que um indicador deixa de significar algo.
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleFilters()

        assertFalse(viewModel.uiState.value.recomputing)
    }

    @Test
    fun `a contagem de filtros diz o que as fileiras fechadas escondem`() = runTest {
        val viewModel = CatalogViewModel(FakeExerciseRepository(exercises = catalogo))
        advanceUntilIdle()

        viewModel.onToggleMuscle("Dorsal")
        viewModel.onToggleLevel(TrainingLevel.ADVANCED)

        assertEquals(2, viewModel.uiState.value.activeFilterCount)

        viewModel.onClearFilters()

        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    private companion object {

        /**
         * Metade da janela de resposta do ViewModel.
         *
         * Serve para pousar **dentro** dela: avançar meia janela duas vezes, com um toque no meio,
         * é o único jeito de distinguir "a janela recomeçou do último toque" de "as duas esperas
         * estão correndo juntas" — que é exatamente o defeito que o cancelamento evita.
         */
        const val HALF_WINDOW_MS = 150L
    }
}
