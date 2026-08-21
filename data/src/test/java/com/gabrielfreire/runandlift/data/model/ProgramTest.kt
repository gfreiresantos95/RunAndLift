package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A condição de um molde poder ir para alguém, e a contagem que a lista mostra.
 *
 * `isAssignable` é o par de `ProgramEditorUiState.canSave`, e a distância entre os dois é a decisão:
 * **salvar exige só o nome, atribuir exige o programa inteiro**. Montar um treino leva dias, e um
 * app que se recusa a guardar trabalho pela metade ensina a pessoa a não confiar nele — mas entregar
 * um dia sem exercício a um aluno é entregar uma tela em branco na academia.
 */
class ProgramTest {

    @Test
    fun `programa completo pode ser atribuido`() {
        assertTrue(program().isAssignable)
    }

    @Test
    fun `programa sem nome nao pode ser atribuido`() {
        // Sem nome, ele não se acha na lista depois.
        assertFalse(program().copy(name = "   ").isAssignable)
    }

    @Test
    fun `programa sem dia nenhum nao pode ser atribuido`() {
        assertFalse(program().copy(days = emptyList()).isAssignable)
    }

    @Test
    fun `um unico dia vazio ja impede a atribuicao`() {
        // É uma promessa vazia para quem abrir o treino na academia, mesmo com os outros dias cheios.
        val program = program().copy(days = listOf(day(), ProgramDay(label = "B")))

        assertFalse(program.isAssignable)
    }

    @Test
    fun `o total de exercicios soma todos os dias`() {
        val program = program().copy(days = listOf(day(), day(label = "B", exercises = 2)))

        assertEquals(3, program.totalExercises)
    }

    @Test
    fun `programa sem dia nao tem exercicio`() {
        assertEquals(0, program().copy(days = emptyList()).totalExercises)
    }

    private fun program() = Program(id = "p1", trainerId = "t1", name = "Treino ABC", days = listOf(day()))

    private fun day(label: String = "A", exercises: Int = 1) = ProgramDay(
        label = label,
        exercises = List(exercises) { index -> prescription("e$index") },
    )

    private fun prescription(id: String) = PrescribedExercise(
        exerciseId = id,
        exerciseName = id,
        sets = PrescribedExercise.DEFAULT_SETS,
        minReps = PrescribedExercise.DEFAULT_MIN_REPS,
        maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
    )
}
