package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.exercise
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
import com.gabrielfreire.runandlift.feature.trainer.fake.MainDispatcherRule
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * O que o preview do editor não mostra: a leitura, a gravação, e o que acontece quando qualquer uma
 * das duas não vai.
 *
 * Duas regras sustentam esta tela e nenhuma delas se vê desenhada. A primeira é que **falha de
 * leitura e programa inexistente caem no mesmo lugar**: abrir o formulário em branco no lugar de um
 * programa que não pôde ser lido faria o treinador remontá-lo por cima do que continua gravado. A
 * segunda é que **o programa novo volta da escrita com o id em memória** — sem isso, um segundo
 * toque em salvar antes de a navegação acontecer criaria um segundo documento.
 *
 * Os testes assinam `uiState` antes de agir porque ele é um `stateIn(WhileSubscribed)`: sem
 * coletor, o `combine` nunca roda e o estado ficaria parado no valor inicial — que é justamente o
 * que faz a tela viver enquanto se anda entre o programa, o dia e a prescrição.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgramEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `programa novo abre pronto, sem leitura e sem indicador`() = runTest {
        val programs = FakeProgramRepository()
        val viewModel = viewModel(programs, programId = TrainerRoutes.NEW_PROGRAM)
        observe(viewModel)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.notFound)
        assertEquals("", viewModel.uiState.value.program.name)
    }

    @Test
    fun `programa existente comeca carregando`() = runTest {
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program())))

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `o programa lido vira o rascunho`() = runTest {
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program())))
        observe(viewModel)

        advanceUntilIdle()

        assertEquals("Treino ABC", viewModel.uiState.value.program.name)
        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.notFound)
    }

    @Test
    fun `programa que nao existe vira notFound, e nao formulario em branco`() = runTest {
        val viewModel = viewModel(FakeProgramRepository())
        observe(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notFound)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `leitura que falha cai no mesmo notFound`() = runTest {
        // É a decisão do KDoc: um editor em branco no lugar de um programa que não pôde ser lido
        // faria o treinador remontá-lo por cima do que continua gravado.
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program()), failReading = true))
        observe(viewModel)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notFound)
    }

    @Test
    fun `salvar grava e avisa quem chamou`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program()))
        val viewModel = viewModel(programs)
        observe(viewModel)
        advanceUntilIdle()

        var avisado = false
        viewModel.save { avisado = true }
        advanceUntilIdle()

        assertTrue(avisado)
        assertEquals("Treino ABC", programs.saved?.name)
        assertFalse(viewModel.uiState.value.saving)
        assertFalse(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun `o dono do programa e quem esta logado`() = runTest {
        val programs = FakeProgramRepository()
        val viewModel = viewModel(programs, programId = TrainerRoutes.NEW_PROGRAM)
        observe(viewModel)
        advanceUntilIdle()

        viewModel.draft.onNameChange("Treino novo")
        // O estado é um `combine` de duas fontes: o nome digitado só chega a `uiState` no próximo
        // despacho, e é dele que `save` lê o que vai gravar.
        advanceUntilIdle()

        viewModel.save {}
        advanceUntilIdle()

        assertEquals(FakeAuthRepository.ACCOUNT.uid, programs.saved?.trainerId)
    }

    @Test
    fun `o programa recem-criado volta com o id em memoria`() = runTest {
        // Sem isso, um segundo toque em salvar antes de a navegação acontecer criaria um segundo
        // documento — o `document()` sorteia um id novo a cada escrita sem id.
        val programs = FakeProgramRepository()
        val viewModel = viewModel(programs, programId = TrainerRoutes.NEW_PROGRAM)
        observe(viewModel)
        advanceUntilIdle()

        viewModel.draft.onNameChange("Treino novo")
        advanceUntilIdle()

        viewModel.save {}
        advanceUntilIdle()
        viewModel.save {}
        advanceUntilIdle()

        assertEquals(FakeProgramRepository.NEW_ID, viewModel.uiState.value.program.id)
        assertEquals(2, programs.saveCount)
        assertEquals(1, programs.programs(FakeAuthRepository.ACCOUNT.uid).size)
    }

    @Test
    fun `programa sem nome nao grava`() = runTest {
        val programs = FakeProgramRepository()
        val viewModel = viewModel(programs, programId = TrainerRoutes.NEW_PROGRAM)
        observe(viewModel)
        advanceUntilIdle()

        var avisado = false
        viewModel.save { avisado = true }
        advanceUntilIdle()

        assertFalse(avisado)
        assertEquals(0, programs.saveCount)
    }

    @Test
    fun `salvar sem rede acende o aviso e a tela nao fecha`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program()), failWriting = true)
        val viewModel = viewModel(programs)
        observe(viewModel)
        advanceUntilIdle()

        var avisado = false
        viewModel.save { avisado = true }
        advanceUntilIdle()

        assertFalse("desempilhar fingindo que gravou é o pior desfecho", avisado)
        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.saving)
    }

    @Test
    fun `salvar sem sessao acende o aviso`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program()))
        val viewModel = ProgramEditorViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            programRepository = programs,
            exerciseRepository = FakeExerciseRepository(),
            programId = "p1",
        )
        observe(viewModel)
        advanceUntilIdle()

        viewModel.save {}
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveFailed)
        assertEquals(0, programs.saveCount)
    }

    @Test
    fun `o exercicio escolhido no catalogo entra no dia que o pediu`() = runTest {
        val catalog = FakeExerciseRepository(exercises = listOf(exercise("agachamento")))
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program())), catalog = catalog)
        observe(viewModel)
        advanceUntilIdle()

        viewModel.addExerciseFromCatalog(dayIndex = 0, exerciseId = "agachamento")
        advanceUntilIdle()

        val exercicios = viewModel.uiState.value.program.days.first().exercises

        assertEquals(listOf("supino", "agachamento"), exercicios.map { it.exerciseId })
    }

    @Test
    fun `id que nao esta no catalogo local nao acrescenta exercicio fantasma`() = runTest {
        // Acontece se o catálogo global for republicado entre a escolha e a volta.
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program())))
        observe(viewModel)
        advanceUntilIdle()

        viewModel.addExerciseFromCatalog(dayIndex = 0, exerciseId = "inexistente")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.program.days.first().exercises.size)
    }

    /**
     * Assina o estado combinado, como a tela faz.
     *
     * `backgroundScope` porque o coletor nunca termina: sem ele, `runTest` esperaria para sempre.
     */
    private fun TestScope.observe(viewModel: ProgramEditorViewModel) {
        backgroundScope.launch { viewModel.uiState.collect {} }
    }

    private fun viewModel(
        programs: FakeProgramRepository,
        catalog: FakeExerciseRepository = FakeExerciseRepository(),
        programId: String = "p1",
    ) = ProgramEditorViewModel(
        authRepository = FakeAuthRepository(),
        programRepository = programs,
        exerciseRepository = catalog,
        programId = programId,
    )
}
