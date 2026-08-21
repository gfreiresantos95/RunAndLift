package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O molde virando a prescrição de uma pessoa.
 *
 * `from` é onde a cópia congela, e é a decisão de arquitetura mais consequente da montagem de
 * treino: a regra de `programs` é `allow read: if isSelf(resource.data.trainerId)`, ou seja, **o
 * aluno não consegue ler a coleção de programas**. Sem a cópia, ele não teria como ler o próprio
 * treino. O que se afirma aqui é que ela é de fato uma cópia — os dias viajam junto —, porque uma
 * versão que só guardasse `programId` passaria em qualquer teste de tela e falharia na academia.
 *
 * O id determinístico é a outra metade: `{trainerId}_{studentId}` é o que faz um aluno ter **um**
 * treino por treinador, e é o que resolve de graça o problema de não atribuir duas vezes.
 */
class AssignmentTest {

    private val program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC",
        goal = TrainingGoal.HYPERTROPHY,
        notes = "Aquecer 10 min",
        days = listOf(day("A"), day("B")),
    )

    @Test
    fun `a prescricao carrega os dias, e nao um ponteiro para o molde`() {
        val assignment = Assignment.from(program, studentId = "a1", studentName = "Ana Souza")

        assertEquals(program.days, assignment.days)
        assertEquals("Treino ABC", assignment.programName)
        assertEquals(TrainingGoal.HYPERTROPHY, assignment.goal)
        assertEquals("Aquecer 10 min", assignment.notes)
    }

    @Test
    fun `o dono da prescricao e o dono do molde`() {
        val assignment = Assignment.from(program, studentId = "a1", studentName = "Ana Souza")

        assertEquals("t1", assignment.trainerId)
        assertEquals("a1", assignment.studentId)
        assertEquals("Ana Souza", assignment.studentName)
        assertEquals("p1", assignment.programId)
    }

    @Test
    fun `a prescricao nasce ativa`() {
        val assignment = Assignment.from(program, studentId = "a1", studentName = "Ana Souza")

        assertTrue(assignment.isActive)
        assertFalse(assignment.copy(status = AssignmentStatus.ENDED).isActive)
    }

    @Test
    fun `o id e sempre treinador e aluno, nessa ordem`() {
        // Determinístico porque Security Rule não consulta, só faz `get()` por caminho exato.
        assertEquals("t1_a1", Assignment.id(trainerId = "t1", studentId = "a1"))
        assertEquals("a1_t1", Assignment.id(trainerId = "a1", studentId = "t1"))
    }

    @Test
    fun `o total de exercicios soma todos os dias da copia`() {
        val assignment = Assignment.from(program, studentId = "a1", studentName = "Ana Souza")

        assertEquals(2, assignment.totalExercises)
    }

    private fun day(label: String) = ProgramDay(
        label = label,
        exercises = listOf(
            PrescribedExercise(
                exerciseId = "supino",
                exerciseName = "Supino reto",
                sets = PrescribedExercise.DEFAULT_SETS,
                minReps = PrescribedExercise.DEFAULT_MIN_REPS,
                maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
            ),
        ),
    )
}
