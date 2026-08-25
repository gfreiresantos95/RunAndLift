package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os dois vazios do catálogo, que são duas telas diferentes — e o vocabulário dos chips, que não
 * pode encolher enquanto se digita.
 *
 * "Não achei nada com esses filtros" se resolve mudando a busca; "o catálogo ainda não chegou" se
 * resolve sincronizando. Desenhar os dois iguais manda o treinador apagar a busca para tentar de
 * novo — e continuar sem nada. É o mesmo erro que a carteira de alunos evita ao separar lista vazia
 * de leitura que falhou.
 */
class CatalogUiStateTest {

    private val exercises = listOf(exercise("supino"), exercise("agachamento"))

    @Test
    fun `sem nada em disco e sem busca, falta o catalogo`() {
        val state = CatalogUiState(loading = false)

        assertTrue(state.isCatalogMissing)
        assertFalse(state.isEmptySearch)
    }

    @Test
    fun `busca que nao achou nada nao e catalogo ausente`() {
        val state = CatalogUiState(loading = false, catalog = exercises, query = "zzz")

        assertFalse(
            "mandar sincronizar quem só digitou errado é o pior conselho possível",
            state.isCatalogMissing,
        )
        assertTrue(state.isEmptySearch)
    }

    @Test
    fun `filtro que fechou demais tambem e busca sem resultado`() {
        val state = CatalogUiState(
            loading = false,
            catalog = exercises,
            results = exercises,
            filter = CatalogFilter(levels = setOf(TrainingLevel.ADVANCED)),
        )

        assertTrue(state.exercises.isEmpty())
        assertFalse(state.isCatalogMissing)
        assertTrue(state.isEmptySearch)
    }

    @Test
    fun `com catalogo e sem filtro, nenhum dos dois vazios aparece`() {
        val state = CatalogUiState(loading = false, catalog = exercises, results = exercises)

        assertFalse(state.isCatalogMissing)
        assertFalse(state.isEmptySearch)
        assertEquals(exercises, state.exercises)
    }

    @Test
    fun `as opcoes de filtro saem do proprio catalogo, sem repetir`() {
        val state = CatalogUiState(loading = false, catalog = exercises, results = exercises)

        assertEquals(
            "uma segunda lista fixa no código divergiria do importador",
            listOf("Peitoral", "Quadríceps"),
            state.muscleOptions,
        )
        assertEquals(listOf("Barra"), state.equipmentOptions)
    }

    @Test
    fun `as opcoes de filtro nao encolhem enquanto se digita`() {
        // Enquanto elas saíam do resultado da busca, procurar "supino" apagava o chip de
        // "Quadríceps" — a pessoa via as opções de filtro sumirem justamente enquanto procurava.
        val state = CatalogUiState(
            loading = false,
            catalog = exercises,
            query = "supino",
            results = listOf(exercises.first()),
        )

        assertEquals(listOf("Peitoral", "Quadríceps"), state.muscleOptions)
    }

    @Test
    fun `a contagem de filtros soma os quatro assuntos`() {
        // É o número que a linha fechada mostra: sem ele, esconder as fileiras esconderia também o
        // que está encolhendo a lista.
        val state = CatalogUiState(
            loading = false,
            catalog = exercises,
            results = exercises,
            filter = CatalogFilter(
                muscleGroups = setOf("Peitoral", "Quadríceps"),
                levels = setOf(TrainingLevel.ADVANCED),
            ),
        )

        assertEquals(3, state.activeFilterCount)
    }

    private fun exercise(id: String) = Exercise(
        id = id,
        name = id,
        muscleGroups = if (id == "supino") listOf("Peitoral") else listOf("Quadríceps"),
        equipment = "Barra",
        instructions = listOf("Execute."),
        level = TrainingLevel.INTERMEDIATE,
    )
}
