package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O que a linha de um dia mostra sem abrir o dia.
 *
 * `totalSets` é a medida de volume que cabe numa linha de lista, e por isso soma as séries e não os
 * exercícios: quatro exercícios de três séries e três de quatro dão o mesmo dia de trabalho, e é
 * esse número que o treinador compara entre um dia e outro.
 */
class ProgramDayTest {

    @Test
    fun `dia sem exercicio esta vazio`() {
        // É estado normal enquanto se monta, e bloqueio na hora de atribuir.
        assertTrue(ProgramDay(label = "A").isEmpty)
        assertFalse(ProgramDay(label = "A", exercises = listOf(prescription(sets = 3))).isEmpty)
    }

    @Test
    fun `o volume do dia soma as series de cada exercicio`() {
        val day = ProgramDay(
            label = "A",
            exercises = listOf(prescription(sets = 3), prescription(sets = 4)),
        )

        assertEquals(7, day.totalSets)
    }

    @Test
    fun `dia vazio tem volume zero`() {
        assertEquals(0, ProgramDay(label = "A").totalSets)
    }

    private fun prescription(sets: Int) = PrescribedExercise(
        exerciseId = "supino",
        exerciseName = "Supino reto",
        sets = sets,
        minReps = PrescribedExercise.DEFAULT_MIN_REPS,
        maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
    )
}
