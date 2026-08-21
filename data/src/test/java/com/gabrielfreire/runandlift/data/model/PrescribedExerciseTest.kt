package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A faixa de repetições que na verdade é um número só.
 *
 * A faixa são dois inteiros e não um texto porque o app precisa somar volume e, mais adiante,
 * comparar o prescrito com o executado — coisa que "8-12" escrito à mão não permite. O preço é este
 * caso: quem quer número fixo põe o mesmo valor nos dois, e sem [PrescribedExercise.hasFixedReps] a
 * tela escreveria "10 a 10" na academia.
 */
class PrescribedExerciseTest {

    @Test
    fun `min igual a max e repeticao fixa`() {
        assertTrue(prescription(minReps = 10, maxReps = 10).hasFixedReps)
    }

    @Test
    fun `faixa de verdade nao e repeticao fixa`() {
        assertFalse(prescription(minReps = 8, maxReps = 12).hasFixedReps)
    }

    private fun prescription(minReps: Int, maxReps: Int) = PrescribedExercise(
        exerciseId = "supino",
        exerciseName = "Supino reto",
        sets = PrescribedExercise.DEFAULT_SETS,
        minReps = minReps,
        maxReps = maxReps,
    )
}
