package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAssignmentRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.day
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O caminho de volta que faltava depois de atribuir: reler o que um aluno recebeu.
 *
 * O que um preview não mostra é a diferença entre **não ter prescrito nada** e **não ter conseguido
 * ler**. As duas desenham a mesma tela vazia, e a primeira frase dita a quem já prescreveu leva o
 * treinador a atribuir de novo — o que substitui a cópia congelada que o aluno está usando.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentWorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `o treino do aluno chega inteiro numa leitura`() = runTest {
        // Dias e exercícios vêm dentro do mesmo documento: abrir todos os dias custa o mesmo que
        // abrir nenhum, que é o que a cópia congelada existe para permitir.
        val viewModel = viewModel(FakeAssignmentRepository(assignments = listOf(assignment())))

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals("Treino ABC", state.assignment?.programName)
        assertEquals(2, state.days.size)
        assertEquals("Ana Ribeiro", state.studentName)
        assertFalse(state.loading)
    }

    @Test
    fun `comeca carregando antes de a leitura responder`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository(assignments = listOf(assignment())))

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `aluno que nunca recebeu nada abre a tela vazia, e nao em falha`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository())

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `leitura que falha nao vira -este aluno nao tem treino-`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository(failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(
            "dizer que não há treino faria o treinador substituir o que o aluno está usando",
            viewModel.uiState.value.isEmpty,
        )
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `o treino de outro treinador nao aparece aqui`() = runTest {
        // A consulta filtra pelos dois identificadores, e não é desempenho: é o que a regra de
        // `assignments` consegue autorizar.
        val viewModel = viewModel(
            FakeAssignmentRepository(assignments = listOf(assignment(trainerId = "outro"))),
        )

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.assignment)
    }

    @Test
    fun `tentar de novo repete a leitura`() = runTest {
        val repository = FakeAssignmentRepository(failReading = true)
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        repository.failReading = false
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `sem sessao nao ha treino a mostrar`() = runTest {
        val viewModel = StudentWorkoutViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            assignmentRepository = FakeAssignmentRepository(assignments = listOf(assignment())),
            studentId = STUDENT_ID,
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    private fun viewModel(assignments: FakeAssignmentRepository) = StudentWorkoutViewModel(
        authRepository = FakeAuthRepository(),
        assignmentRepository = assignments,
        studentId = STUDENT_ID,
    )

    private fun assignment(trainerId: String = FakeAuthRepository.ACCOUNT.uid) = Assignment(
        trainerId = trainerId,
        studentId = STUDENT_ID,
        studentName = "Ana Ribeiro",
        programId = "p1",
        programName = "Treino ABC",
        days = listOf(day("A"), day("B")),
        status = AssignmentStatus.ACTIVE,
    )

    private companion object {
        const val STUDENT_ID = "a1"
    }
}
