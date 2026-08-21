package com.gabrielfreire.runandlift.data.assignment

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A prescrição indo para o Firestore e voltando, e a cópia que a torna possível.
 *
 * O teste que mais importa aqui é o da **cópia congelada**: é ela que permite ao aluno ler o próprio
 * treino sem poder ler a coleção de programas, e um dia perdido no caminho vira um treino incompleto
 * na academia, sem erro nenhum aparecendo em lugar algum.
 *
 * O segundo é o do **id determinístico**. `{trainerId}_{studentId}` é o que faz atribuir duas vezes
 * substituir em vez de duplicar — sem ele, um toque a mais deixaria o aluno com dois treinos ativos
 * e a tela dele teria de escolher um.
 */
class AssignmentDocumentTest {

    private val program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC",
        goal = TrainingGoal.HYPERTROPHY,
        notes = "Progredir carga a cada duas semanas",
        days = listOf(
            ProgramDay(
                label = "A",
                focus = "Peito e tríceps",
                exercises = listOf(
                    PrescribedExercise(
                        exerciseId = "supino",
                        exerciseName = "Supino reto",
                        sets = 4,
                        minReps = 8,
                        maxReps = 12,
                        loadKg = 60.0,
                        restSeconds = 90,
                        notes = "Desça devagar",
                    ),
                ),
            ),
        ),
    )

    private val assignment = Assignment.from(program = program, studentId = "a1", studentName = "Ana")

    @Test
    fun `o id e sempre treinador e aluno, e nao inclui o programa`() {
        assertEquals(
            "atribuir de novo tem de substituir, e não deixar o aluno com dois treinos ativos",
            "t1_a1",
            Assignment.id(trainerId = "t1", studentId = "a1"),
        )
    }

    @Test
    fun `a copia leva os dias inteiros, com a prescricao de cada exercicio`() {
        assertEquals(program.days, assignment.days)

        val exercise = assignment.days.first().exercises.first()
        assertEquals(4, exercise.sets)
        assertEquals(60.0, exercise.loadKg!!, 0.0)
        assertEquals("Desça devagar", exercise.notes)
    }

    @Test
    fun `a copia guarda de onde veio, e nasce ativa`() {
        assertEquals("p1", assignment.programId)
        assertEquals("Treino ABC", assignment.programName)
        assertEquals(TrainingGoal.HYPERTROPHY, assignment.goal)
        assertTrue(assignment.isActive)
    }

    @Test
    fun `editar o programa depois nao alcanca quem ja recebeu`() {
        // É a consequência declarada da cópia congelada: reatribuir é o que atualiza.
        val edited = program.copy(name = "Outro nome", days = emptyList())

        assertEquals("Treino ABC", assignment.programName)
        assertEquals(1, assignment.days.size)
        assertEquals(0, edited.days.size)
    }

    @Test
    fun `ida e volta preserva a prescricao`() {
        val restored = AssignmentDocument.assignment(AssignmentDocument.toMap(assignment))

        assertEquals(assignment.copy(updatedAt = 0L), restored)
    }

    @Test
    fun `a situacao e gravada em minusculo, como as regras comparam`() {
        val map = AssignmentDocument.toMap(assignment.copy(status = AssignmentStatus.ENDED))

        assertEquals("ended", map["status"])
    }

    @Test
    fun `o carimbo de tempo nao e gravado pelo cliente`() {
        assertTrue(AssignmentDocument.FIELD_UPDATED_AT !in AssignmentDocument.toMap(assignment).keys)
    }

    @Test
    fun `documento sem aluno nao vira prescricao`() {
        val map = AssignmentDocument.toMap(assignment).toMutableMap().apply { remove("studentId") }

        assertNull(AssignmentDocument.assignment(map))
    }

    @Test
    fun `documento inexistente nao vira prescricao`() {
        assertNull(AssignmentDocument.assignment(null))
    }

    @Test
    fun `situacao desconhecida vale como ativa, e o treino nao some do aluno`() {
        val map = AssignmentDocument.toMap(assignment) + ("status" to "arquivado")

        assertTrue(
            "esconder o treino de alguém por causa de um campo torto é o pior desfecho",
            AssignmentDocument.assignment(map)?.isActive == true,
        )
    }

    @Test
    fun `dia quebrado some sem levar o treino junto`() {
        val map = AssignmentDocument.toMap(assignment) +
            ("days" to listOf(mapOf("focus" to "sem rótulo"), mapOf("label" to "B")))

        val restored = AssignmentDocument.assignment(map)

        assertEquals(1, restored?.days?.size)
        assertEquals("B", restored?.days?.first()?.label)
    }
}
