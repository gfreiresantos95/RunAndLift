package com.gabrielfreire.runandlift.feature.trainer.assign

import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository.Companion.link
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As três perguntas que a linha de cada aluno faz ao estado antes de se desenhar.
 *
 * São as decisões que o `@Preview` não alcança porque dependem de qual aluno é: a fixture desenha
 * uma lista, e o que se verifica aqui é que a marcação cai no aluno certo — trocar `assignedIds` por
 * um booleano de tela poria a etiqueta "já recebeu" na linha inteira.
 */
class AssignUiStateTest {

    private val ana = link(LinkStatus.ACTIVE)
    private val bruno = link(LinkStatus.ACTIVE, name = "Bruno")

    @Test
    fun `sem aluno ativo a lista esta vazia`() {
        assertTrue(AssignUiState().isEmpty)
        assertFalse(AssignUiState(students = listOf(ana)).isEmpty)
    }

    @Test
    fun `so quem esta em assignedIds aparece marcado`() {
        val state = AssignUiState(students = listOf(ana, bruno), assignedIds = setOf(ana.studentId))

        assertTrue(state.isAssigned(ana))
        assertFalse(state.isAssigned(bruno))
    }

    @Test
    fun `so a linha em gravacao fica travada`() {
        // O travamento é por aluno, e não da tela toda: travar tudo faria a lista inteira piscar
        // enquanto uma escrita não volta.
        val state = AssignUiState(students = listOf(ana, bruno), assigning = ana.studentId)

        assertTrue(state.isAssigning(ana))
        assertFalse(state.isAssigning(bruno))
    }

    @Test
    fun `sem gravacao em curso nenhuma linha fica travada`() {
        val state = AssignUiState(students = listOf(ana, bruno))

        assertFalse(state.isAssigning(ana))
        assertFalse(state.isAssigning(bruno))
    }
}
