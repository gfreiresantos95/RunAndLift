package com.gabrielfreire.runandlift.data.program

import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O programa indo para o Firestore e voltando.
 *
 * É o teste que a tela não faz: um `@Preview` mostra o editor bonito e não diz nada sobre o que foi
 * gravado. E o que se grava aqui é um documento aninhado em três níveis — programa, dia, exercício
 * —, que é exatamente a forma em que um campo esquecido some sem erro nenhum.
 *
 * As duas regras que mais importam estão nos testes de leitura defensiva: **um item quebrado não
 * pode custar o programa inteiro**, e um número ausente cai no padrão em vez de descartar o
 * exercício. Um treinador que perdeu quarenta exercícios porque um deles veio sem `sets` não volta.
 */
class ProgramDocumentTest {

    private val program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC",
        goal = TrainingGoal.HYPERTROPHY,
        notes = "Progredir carga a cada duas semanas",
        days = listOf(
            ProgramDay(
                label = "A",
                focus = "Peito e tríceps",
                exercises = listOf(
                    PrescribedExercise(
                        exerciseId = "supino",
                        exerciseName = "Supino reto",
                        sets = 4,
                        minReps = 8,
                        maxReps = 12,
                        loadKg = 60.0,
                        restSeconds = 90,
                        notes = "Desça devagar",
                    ),
                ),
            ),
        ),
    )

    @Test
    fun `ida e volta preserva o programa`() {
        val restored = ProgramDocument.program(id = "p1", data = ProgramDocument.toMap(program))

        assertEquals(program.copy(updatedAt = 0L), restored)
    }

    @Test
    fun `o carimbo de tempo nao e gravado pelo cliente`() {
        // Quem o escreve é o repositório, com o relógio do servidor. Um valor daqui ordenaria a
        // lista pelo relógio do aparelho, e quem está com a data trocada iria para o topo.
        assertTrue(ProgramDocument.FIELD_UPDATED_AT !in ProgramDocument.toMap(program).keys)
    }

    @Test
    fun `texto em branco vira ausencia, e nao string vazia`() {
        val map = ProgramDocument.toMap(program.copy(notes = "   "))

        assertNull("observação só de espaço é ausência de observação", map["notes"])
    }

    @Test
    fun `nome e recortado antes de gravar`() {
        val map = ProgramDocument.toMap(program.copy(name = "  Treino ABC  "))

        assertEquals("Treino ABC", map["name"])
    }

    @Test
    fun `documento sem treinador nao vira programa`() {
        val map = ProgramDocument.toMap(program).toMutableMap().apply { remove("trainerId") }

        assertNull(ProgramDocument.program(id = "p1", data = map))
    }

    @Test
    fun `documento sem nome nao vira programa`() {
        val map = ProgramDocument.toMap(program).toMutableMap().apply { remove("name") }

        assertNull(ProgramDocument.program(id = "p1", data = map))
    }

    @Test
    fun `documento inexistente nao vira programa`() {
        assertNull(ProgramDocument.program(id = "p1", data = null))
    }

    @Test
    fun `exercicio sem id some sem levar o dia junto`() {
        val map = mapOf(
            "trainerId" to "t1",
            "name" to "Treino",
            "days" to listOf(
                mapOf(
                    "label" to "A",
                    "exercises" to listOf(
                        mapOf("exerciseName" to "Sem id"),
                        mapOf("exerciseId" to "supino", "exerciseName" to "Supino", "sets" to 4),
                    ),
                ),
            ),
        )

        val restored = ProgramDocument.program(id = "p1", data = map)

        assertEquals(1, restored?.days?.first()?.exercises?.size)
        assertEquals("supino", restored?.days?.first()?.exercises?.first()?.exerciseId)
    }

    @Test
    fun `dia sem rotulo some sem levar o programa junto`() {
        val map = mapOf(
            "trainerId" to "t1",
            "name" to "Treino",
            "days" to listOf(mapOf("focus" to "sem rótulo"), mapOf("label" to "B")),
        )

        val restored = ProgramDocument.program(id = "p1", data = map)

        assertEquals(1, restored?.days?.size)
        assertEquals("B", restored?.days?.first()?.label)
    }

    @Test
    fun `numero ausente cai no padrao em vez de descartar o exercicio`() {
        val map = mapOf(
            "trainerId" to "t1",
            "name" to "Treino",
            "days" to listOf(
                mapOf(
                    "label" to "A",
                    "exercises" to listOf(mapOf("exerciseId" to "supino", "exerciseName" to "Supino")),
                ),
            ),
        )

        val exercise = ProgramDocument.program(id = "p1", data = map)?.days?.first()?.exercises?.first()

        assertEquals(PrescribedExercise.DEFAULT_SETS, exercise?.sets)
        assertEquals(PrescribedExercise.DEFAULT_MIN_REPS, exercise?.minReps)
        assertEquals(PrescribedExercise.DEFAULT_MAX_REPS, exercise?.maxReps)
    }

    @Test
    fun `faixa invertida e corrigida, e nao mostrada de cabeca para baixo`() {
        val map = mapOf(
            "trainerId" to "t1",
            "name" to "Treino",
            "days" to listOf(
                mapOf(
                    "label" to "A",
                    "exercises" to listOf(
                        mapOf("exerciseId" to "x", "exerciseName" to "X", "minReps" to 12, "maxReps" to 8),
                    ),
                ),
            ),
        )

        val exercise = ProgramDocument.program(id = "p1", data = map)?.days?.first()?.exercises?.first()

        assertEquals(12, exercise?.minReps)
        assertEquals("o maior nunca é menor que o menor", 12, exercise?.maxReps)
    }

    @Test
    fun `objetivo desconhecido vira ausencia, e o programa entra`() {
        val map = ProgramDocument.toMap(program) + ("goal" to "CROSSFIT")

        val restored = ProgramDocument.program(id = "p1", data = map)

        assertEquals("Treino ABC", restored?.name)
        assertNull(restored?.goal)
    }
}
