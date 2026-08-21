package com.gabrielfreire.runandlift.feature.student.workouts

import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.feature.student.fake.FakeAssignmentRepository
import com.gabrielfreire.runandlift.feature.student.fake.FakeAssignmentRepository.Companion.assignment
import com.gabrielfreire.runandlift.feature.student.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.student.fake.MainDispatcherRule
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
 * O que o preview da aba não mostra: de onde vem o treino, e o que sobra na tela quando ele não vem.
 *
 * O teste que mais importa é o da falha, e aqui ele pesa mais do que nas outras telas: **"seu
 * treinador ainda não montou seu treino" é uma frase convincente**. Dita a quem tem treino e está
 * sem sinal, ela manda a pessoa cobrar alguém por algo que já foi feito — nas outras telas o vazio
 * apenas confunde, nesta ele acusa.
 *
 * O segundo é o do custo: abrir a aba custa **uma** leitura, e abrir os dias não custa nenhuma. É o
 * que a prescrição carregar os dias dentro de si existe para render (§2.4, regra 2).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StudentWorkoutsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `o treino prescrito aparece no estado`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository(assignment()))

        advanceUntilIdle()

        assertEquals("Full body iniciante", viewModel.uiState.value.assignment?.programName)
        assertEquals(3, viewModel.uiState.value.days.size)
        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository(assignment()))

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `aluno sem treino tem a aba vazia, e nao uma falha`() = runTest {
        val viewModel = viewModel(FakeAssignmentRepository())

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `leitura que falha vira falha, e nao aba vazia`() = runTest {
        // A pior frase que esta tela pode produzir é "seu treinador ainda não montou seu treino"
        // dita a quem tem treino e está sem sinal.
        val viewModel = viewModel(FakeAssignmentRepository(assignment(), failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `o treino que ja estava na tela sobrevive a uma releitura que falha`() = runTest {
        val repository = FakeAssignmentRepository(assignment())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        // A rede cai com a aba aberta: quem já via o treino não o perde porque uma releitura não
        // respondeu. É o que faz o treino continuar legível no vestiário sem sinal.
        repository.failReading = true
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("Full body iniciante", viewModel.uiState.value.assignment?.programName)
        assertTrue(viewModel.uiState.value.failed)
    }

    @Test
    fun `tentar de novo limpa a falha quando a rede volta`() = runTest {
        val repository = FakeAssignmentRepository(assignment(), failReading = true)
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        repository.failReading = false
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.failed)
        assertEquals(3, viewModel.uiState.value.days.size)
    }

    @Test
    fun `treino encerrado nao e treino, e a aba fica vazia`() = runTest {
        // Encerrado e ausente são coisas diferentes no documento, mas a aba mostra o treino de
        // agora: um programa que acabou não é o que se abre na academia.
        val encerrado = assignment(status = AssignmentStatus.ENDED)
        val viewModel = viewModel(FakeAssignmentRepository(encerrado))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `sem sessao nao ha treino, e a tela nao fica presa carregando`() = runTest {
        val viewModel = StudentWorkoutsViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            assignmentRepository = FakeAssignmentRepository(assignment()),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
        assertNull(viewModel.uiState.value.assignment)
    }

    @Test
    fun `abrir a aba custa uma leitura, e abrir os dias nao custa nenhuma`() = runTest {
        val repository = FakeAssignmentRepository(assignment())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        // Os dias saem do mesmo estado — é o documento único da prescrição rendendo o que promete.
        repeat(times = 3) { index -> viewModel.uiState.value.day(index) }
        advanceUntilIdle()

        assertEquals(1, repository.readCount)
    }

    private fun viewModel(assignments: FakeAssignmentRepository) = StudentWorkoutsViewModel(
        authRepository = FakeAuthRepository(),
        assignmentRepository = assignments,
    )
}
