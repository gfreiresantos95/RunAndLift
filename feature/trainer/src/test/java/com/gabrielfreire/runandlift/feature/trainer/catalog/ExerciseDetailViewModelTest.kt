package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.exercise
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
 * A ficha de um exercício, e a diferença entre "ainda lendo" e "não está no catálogo".
 *
 * As duas seriam a mesma tela em branco se `loading` e `exercise` fossem um estado só — e a segunda
 * acontece de verdade: o catálogo global é republicado de fora do app, e um exercício pode sair dele
 * entre uma versão e outra. A tela diz isso em vez de mostrar uma ficha vazia.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `o exercicio do catalogo local abre sem tocar a rede`() = runTest {
        val viewModel = viewModel(exerciseId = "supino")

        advanceUntilIdle()

        assertEquals("Supino", viewModel.exercise.value?.name)
        assertFalse(viewModel.loading.value)
    }

    @Test
    fun `comeca carregando antes de o banco responder`() = runTest {
        val viewModel = viewModel(exerciseId = "supino")

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.loading.value)
        assertNull(viewModel.exercise.value)
    }

    @Test
    fun `exercicio que saiu do catalogo termina de carregar como ausente`() = runTest {
        // Sem o `loading` terminando, a tela ficaria girando para sempre num exercício que não
        // existe mais — que é o desfecho pior dos dois.
        val viewModel = viewModel(exerciseId = "inexistente")

        advanceUntilIdle()

        assertNull(viewModel.exercise.value)
        assertFalse(viewModel.loading.value)
    }

    private fun viewModel(exerciseId: String) = ExerciseDetailViewModel(
        exerciseRepository = FakeExerciseRepository(exercises = listOf(exercise("supino"))),
        exerciseId = exerciseId,
    )
}
