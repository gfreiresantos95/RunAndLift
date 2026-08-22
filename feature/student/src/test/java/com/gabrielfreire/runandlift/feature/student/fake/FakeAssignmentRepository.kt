package com.gabrielfreire.runandlift.feature.student.fake

import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * [AssignmentRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Cópia da do `:feature:trainer`, e não um reuso: os dois módulos não compartilham source set de
 * teste, e o gatilho para um `:test-fixtures` continua sendo o terceiro módulo. **Não é uma cópia
 * idêntica**, e a diferença é o papel: aqui só as leituras têm comportamento, e escrever falha em
 * voz alta — uma tela de aluno que tente prescrever é erro de arquitetura, e a regra do Firestore o
 * recusaria de qualquer forma.
 *
 * [failReading] é `var` porque a rede cai **no meio** de uma sessão: o caso que interessa não é
 * abrir a aba sem sinal, é perder o sinal com o treino já na tela.
 */
internal class FakeAssignmentRepository(private val assignment: Assignment? = null, var failReading: Boolean = false) :
    AssignmentRepository {

    var readCount: Int = 0
        private set

    override suspend fun activeAssignment(studentId: String): Assignment? {
        readCount++

        if (failReading) error("sem rede e sem cache")

        return assignment?.takeIf { it.studentId == studentId && it.isActive }
    }

    override suspend fun assignmentsOfProgram(trainerId: String, programId: String): List<Assignment> = unsupported()

    override suspend fun assign(assignment: Assignment): Assignment = unsupported()

    override suspend fun end(assignment: Assignment) = unsupported()

    private fun unsupported(): Nothing = error("o aluno não prescreve nada")

    companion object {

        /** O aluno das outras fixtures deste módulo, para os ids baterem entre os fakes. */
        const val STUDENT_ID = "u1"

        /** Um treino com três dias, sendo o último sem foco escrito — que é caso comum. */
        fun assignment(
            studentId: String = STUDENT_ID,
            status: AssignmentStatus = AssignmentStatus.ACTIVE,
            days: List<ProgramDay> = listOf(day("A", "Peito e tríceps"), day("B", "Costas"), day("C", null)),
        ) = Assignment(
            trainerId = "t1",
            studentId = studentId,
            studentName = "Ana Souza",
            programId = "p1",
            programName = "Full body iniciante",
            goal = TrainingGoal.HYPERTROPHY,
            notes = "Aquecer 10 minutos antes.",
            days = days,
            status = status,
        )

        /** Um dia com um exercício, que é o mínimo que a atribuição deixa passar. */
        fun day(label: String, focus: String? = null, exercises: Int = 1) = ProgramDay(
            label = label,
            focus = focus,
            exercises = List(exercises) { index -> prescription("e$index") },
        )

        fun prescription(id: String) = PrescribedExercise(
            exerciseId = id,
            exerciseName = id.replaceFirstChar(Char::uppercase),
            sets = PrescribedExercise.DEFAULT_SETS,
            minReps = PrescribedExercise.DEFAULT_MIN_REPS,
            maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
        )
    }
}
