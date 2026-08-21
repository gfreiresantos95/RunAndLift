package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus

/**
 * [AssignmentRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Guarda as prescrições **por id** — `{trainerId}_{studentId}` —, e isso não é detalhe de fake: é o
 * que reproduz a regra que mais importa da coleção, a de que atribuir de novo **substitui** o treino
 * daquele par em vez de acrescentar um segundo. Uma lista simples esconderia exatamente isso.
 *
 * @param failWriting gravação recusada. Aqui é o caso comum, e não o excepcional: prescrever exige
 *   rede, porque a regra do Firestore precisa conferir o vínculo ativo.
 */
internal class FakeAssignmentRepository(
    assignments: List<Assignment> = emptyList(),
    var failReading: Boolean = false,
    var failWriting: Boolean = false,
) : AssignmentRepository {

    private val stored = assignments.associateByTo(mutableMapOf()) { Assignment.id(it.trainerId, it.studentId) }

    var assigned: Assignment? = null
        private set

    var writeCount: Int = 0
        private set

    override suspend fun assignmentsOfProgram(trainerId: String, programId: String): List<Assignment> {
        if (failReading) error("sem rede e sem cache")

        return stored.values.filter { it.trainerId == trainerId && it.programId == programId }
    }

    override suspend fun activeAssignment(studentId: String): Assignment? {
        if (failReading) error("sem rede e sem cache")

        return stored.values.firstOrNull { it.studentId == studentId && it.isActive }
    }

    override suspend fun assign(assignment: Assignment): Assignment {
        if (failWriting) error("gravação não completou")

        writeCount++
        assigned = assignment
        stored[Assignment.id(assignment.trainerId, assignment.studentId)] = assignment

        return assignment
    }

    override suspend fun end(assignment: Assignment) {
        if (failWriting) error("gravação não completou")

        writeCount++

        val id = Assignment.id(assignment.trainerId, assignment.studentId)

        stored[id] = assignment.copy(status = AssignmentStatus.ENDED)
    }
}
