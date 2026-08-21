package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.trainer.catalog.CatalogFilter.Companion.toggled
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os filtros do catálogo.
 *
 * A regra que este arquivo existe para travar é **"nenhum chip marcado significa todos"**. É o
 * oposto do que um `filter` ingênuo faz, e é a diferença entre uma tela que abre com 868 exercícios
 * à mostra e uma que abre em branco esperando o primeiro toque — que quem abrir vai ler como
 * catálogo vazio.
 */
class CatalogFilterTest {

    private val supino = exercise(
        id = "supino",
        muscles = listOf("Peitoral"),
        equipment = "Barra",
        level = TrainingLevel.INTERMEDIATE,
    )
    private val prancha = exercise(
        id = "prancha",
        muscles = listOf("Abdômen"),
        equipment = null,
        level = TrainingLevel.BEGINNER,
    )
    private val alongamento = exercise(
        id = "alongamento",
        muscles = listOf("Lombar"),
        equipment = null,
        level = TrainingLevel.BEGINNER,
        category = ExerciseCategory.STRETCHING,
    )
    private val all = listOf(supino, prancha, alongamento)

    @Test
    fun `sem filtro nenhum a lista passa inteira`() {
        assertEquals(all, CatalogFilter().apply(all))
    }

    @Test
    fun `filtro vazio nao e filtro marcado`() {
        assertFalse(CatalogFilter().isActive)
        assertTrue(CatalogFilter(levels = setOf(TrainingLevel.BEGINNER)).isActive)
    }

    @Test
    fun `categoria filtra por igualdade`() {
        val filtered = CatalogFilter(categories = setOf(ExerciseCategory.STRENGTH)).apply(all)

        assertEquals(listOf(supino, prancha), filtered)
    }

    @Test
    fun `musculo casa quando qualquer um dos marcados for primario`() {
        val filtered = CatalogFilter(muscleGroups = setOf("Peitoral", "Abdômen")).apply(all)

        assertEquals(
            "filtro de músculo é 'peito ou abdômen', nunca os dois ao mesmo tempo",
            listOf(supino, prancha),
            filtered,
        )
    }

    @Test
    fun `exercicio sem equipamento nao passa por filtro de equipamento`() {
        val filtered = CatalogFilter(equipment = setOf("Barra")).apply(all)

        assertEquals(
            "flexão sem equipamento declarado não pode aparecer em quem procurou barra",
            listOf(supino),
            filtered,
        )
    }

    @Test
    fun `os filtros se somam, e nao se substituem`() {
        val filtered = CatalogFilter(
            categories = setOf(ExerciseCategory.STRENGTH),
            levels = setOf(TrainingLevel.BEGINNER),
        ).apply(all)

        assertEquals(listOf(prancha), filtered)
    }

    @Test
    fun `marcar duas vezes desmarca`() {
        val once = emptySet<TrainingLevel>().toggled(TrainingLevel.BEGINNER)
        val twice = once.toggled(TrainingLevel.BEGINNER)

        assertEquals(setOf(TrainingLevel.BEGINNER), once)
        assertEquals(emptySet<TrainingLevel>(), twice)
    }

    private fun exercise(
        id: String,
        muscles: List<String>,
        equipment: String?,
        level: TrainingLevel,
        category: ExerciseCategory = ExerciseCategory.STRENGTH,
    ) = Exercise(
        id = id,
        name = id,
        muscleGroups = muscles,
        equipment = equipment,
        instructions = listOf("Execute."),
        level = level,
        category = category,
    )
}
