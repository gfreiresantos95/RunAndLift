package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.day
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As três situações da tela, que são três conversas diferentes.
 *
 * A que mais importa é a última: **leitura que falhou não é "este aluno não tem treino"**. A frase
 * errada manda o treinador prescrever de novo o que ele já prescreveu — e reatribuir substitui a
 * cópia congelada que o aluno está usando na academia. É o mesmo erro que a carteira evita ao
 * separar carteira vazia de leitura que não respondeu, com uma consequência bem pior.
 */
class StudentWorkoutUiStateTest {

    @Test
    fun `sem atribuicao e sem falha, o aluno nao recebeu treino`() {
        val state = StudentWorkoutUiState(loading = false)

        assertTrue(state.isEmpty)
        assertFalse(state.isEnded)
    }

    @Test
    fun `leitura que falhou nao e -este aluno nao tem treino-`() {
        val state = StudentWorkoutUiState(loading = false, failed = true)

        assertFalse(
            "a frase errada faz o treinador substituir a cópia que o aluno está usando",
            state.isEmpty,
        )
    }

    @Test
    fun `treino encerrado continua na tela, dito com todas as letras`() {
        // O documento que fica é justamente o que permite dizer que o treino acabou, em vez de a
        // tela esvaziar de um dia para o outro.
        val state = StudentWorkoutUiState(loading = false, assignment = assignment(AssignmentStatus.ENDED))

        assertTrue(state.isEnded)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `treino ativo nao se anuncia como encerrado`() {
        val state = StudentWorkoutUiState(loading = false, assignment = assignment(AssignmentStatus.ACTIVE))

        assertFalse(state.isEnded)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `o nome do aluno sai de dentro do documento, e nao da rota`() {
        val state = StudentWorkoutUiState(loading = false, assignment = assignment(AssignmentStatus.ACTIVE))

        assertEquals("Ana Ribeiro", state.studentName)
        assertEquals("", StudentWorkoutUiState(loading = false).studentName)
    }

    @Test
    fun `o tamanho do treino soma os exercicios de todos os dias`() {
        val state = StudentWorkoutUiState(loading = false, assignment = assignment(AssignmentStatus.ACTIVE))

        assertEquals(2, state.days.size)
        assertEquals(2, state.totalExercises)
    }

    private fun assignment(status: AssignmentStatus) = Assignment(
        trainerId = "t1",
        studentId = "a1",
        studentName = "Ana Ribeiro",
        programId = "p1",
        programName = "Treino ABC",
        days = listOf(day("A"), day("B")),
        status = status,
    )
}
