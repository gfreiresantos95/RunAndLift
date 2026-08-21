package com.gabrielfreire.runandlift.feature.trainer.programs

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeAuthRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
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
 * O que o preview da aba de treinos não mostra: o que acontece quando a leitura falha, e o que sobra
 * na lista quando uma exclusão não vai.
 *
 * O teste que mais importa é o mesmo da carteira: **"você ainda não montou nenhum programa" e "não
 * consegui carregar" são a mesma tela em branco**, e a primeira frase dita a quem tem doze programas
 * e está sem sinal esconde que basta tentar de novo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgramsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `os programas lidos aparecem no estado`() = runTest {
        val viewModel = viewModel(FakeProgramRepository(programs = listOf(program(), program(id = "p2"))))

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.programs.size)
        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.failed)
    }

    @Test
    fun `comeca carregando antes de a leitura terminar`() = runTest {
        val viewModel = viewModel(FakeProgramRepository())

        // Sem advanceUntilIdle de propósito: é o primeiro frame, com a tela já desenhada.
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `leitura que falha vira falha, e nao lista vazia`() = runTest {
        val viewModel = viewModel(FakeProgramRepository(failReading = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.failed)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `a lista que ja estava na tela sobrevive a uma releitura que falha`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program()))
        val viewModel = viewModel(programs)
        advanceUntilIdle()

        // A rede cai com a tela aberta, e a pessoa volta do editor: quem já via os programas não os
        // perde porque uma releitura não respondeu.
        programs.failReading = true
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.programs.size)
        assertTrue(viewModel.uiState.value.failed)
    }

    @Test
    fun `sem sessao nao ha programa, e a tela nao fica presa carregando`() = runTest {
        val viewModel = ProgramsViewModel(
            authRepository = FakeAuthRepository(signedIn = null),
            programRepository = FakeProgramRepository(programs = listOf(program())),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun `excluir tira o molde da lista`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program(), program(id = "p2")))
        val viewModel = viewModel(programs)
        advanceUntilIdle()

        viewModel.onDelete(program())
        advanceUntilIdle()

        assertEquals(listOf("p2"), viewModel.uiState.value.programs.map { it.id })
        assertEquals("p1", programs.deleted)
        assertNull(viewModel.uiState.value.deleting)
    }

    @Test
    fun `excluir que falha deixa o programa onde estava`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program()))
        val viewModel = viewModel(programs)
        advanceUntilIdle()

        programs.failWriting = true
        viewModel.onDelete(program())
        advanceUntilIdle()

        // O programa continua ali, que é a verdade — a tela não pode mostrar um estado que o banco
        // não tem.
        assertEquals(1, viewModel.uiState.value.programs.size)
        assertTrue(viewModel.uiState.value.failed)
        assertNull(viewModel.uiState.value.deleting)
    }

    @Test
    fun `uma exclusao por vez`() = runTest {
        val programs = FakeProgramRepository(programs = listOf(program(), program(id = "p2")))
        val viewModel = viewModel(programs)
        advanceUntilIdle()

        viewModel.onDelete(program())
        // Sem esperar a primeira terminar: o segundo toque não pode disparar outra escrita.
        viewModel.onDelete(program(id = "p2"))
        advanceUntilIdle()

        assertEquals("p1", programs.deleted)
        assertEquals(listOf("p2"), viewModel.uiState.value.programs.map { it.id })
    }

    private fun viewModel(programs: FakeProgramRepository) =
        ProgramsViewModel(authRepository = FakeAuthRepository(), programRepository = programs)
}
