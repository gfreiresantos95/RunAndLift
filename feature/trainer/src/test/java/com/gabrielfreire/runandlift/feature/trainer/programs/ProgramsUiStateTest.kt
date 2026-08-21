package com.gabrielfreire.runandlift.feature.trainer.programs

import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As duas perguntas que a aba de treinos faz ao estado antes de se desenhar.
 *
 * `isEmpty` ignora [ProgramsUiState.failed] de propósito: quem decide entre a tela vazia e a de erro
 * é a tela, olhando as duas — o estado só responde o que é verdade sobre a lista, e é por isso que
 * uma falha de leitura não pode fazê-lo dizer "vazio".
 */
class ProgramsUiStateTest {

    @Test
    fun `sem programa a lista esta vazia`() {
        assertTrue(ProgramsUiState().isEmpty)
        assertFalse(ProgramsUiState(programs = listOf(program())).isEmpty)
    }

    @Test
    fun `falha de leitura nao transforma a lista cheia em vazia`() {
        assertFalse(ProgramsUiState(failed = true, programs = listOf(program())).isEmpty)
    }

    @Test
    fun `so a linha em exclusao fica travada`() {
        val state = ProgramsUiState(programs = listOf(program(), program(id = "p2")), deleting = "p1")

        assertTrue(state.isDeleting(program()))
        assertFalse(state.isDeleting(program(id = "p2")))
    }

    @Test
    fun `sem exclusao em curso nenhuma linha fica travada`() {
        assertFalse(ProgramsUiState(programs = listOf(program())).isDeleting(program()))
    }
}
