package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeExerciseRepository.Companion.day
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeProgramRepository.Companion.program
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A diferença entre poder salvar e poder atribuir, que é o coração desta tela.
 *
 * **Salvar exige só o nome.** Montar um programa leva dias, e um app que se recusa a guardar
 * trabalho pela metade ensina a pessoa a não confiar nele — dia vazio e programa sem dia continuam
 * sendo problema, mas na hora de entregar a alguém, que é quando eles realmente atrapalham.
 *
 * **Atribuir exige o programa gravado**, porque a cópia que o aluno recebe sai do documento e não do
 * rascunho: atribuir uma edição que ainda não foi salva daria ao aluno um treino que não existe.
 */
class ProgramEditorUiStateTest {

    @Test
    fun `so o nome trava o salvamento`() {
        assertFalse(state(program().copy(name = " ")).canSave)
        assertTrue(state(program().copy(days = emptyList())).canSave)
        assertTrue(state(program()).canSave)
    }

    @Test
    fun `gravacao em curso trava o botao`() {
        // Dois toques não podem virar dois documentos.
        assertFalse(state(program()).copy(saving = true).canSave)
    }

    @Test
    fun `programa sem dia ou com dia vazio fica incompleto`() {
        assertTrue(state(program().copy(days = emptyList())).incomplete)
        assertTrue(state(program().copy(days = listOf(ProgramDay(label = "A")))).incomplete)
        assertFalse(state(program()).incomplete)
    }

    @Test
    fun `atribuir exige o programa ja gravado`() {
        // O rascunho de um programa novo não tem id: entregá-lo daria ao aluno um treino que não
        // existe em `programs`.
        assertFalse(state(program().copy(id = "")).canAssign)
        assertTrue(state(program()).canAssign)
    }

    @Test
    fun `atribuir exige o programa completo`() {
        assertFalse(state(program().copy(days = emptyList())).canAssign)
        assertFalse(state(program().copy(days = listOf(day(), ProgramDay(label = "B")))).canAssign)
    }

    @Test
    fun `gravacao em curso tambem trava a atribuicao`() {
        assertFalse(state(program()).copy(saving = true).canAssign)
    }

    private fun state(program: Program) = ProgramEditorUiState(program = program)
}
