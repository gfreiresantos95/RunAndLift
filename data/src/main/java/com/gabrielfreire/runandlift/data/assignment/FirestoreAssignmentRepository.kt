package com.gabrielfreire.runandlift.data.assignment

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * [AssignmentRepository] sobre o Firestore.
 *
 * O que ficou aqui é conversa com o SDK. O mapa de cada documento — e o que acontece com um que
 * chegue quebrado — mora em [AssignmentDocument], onde um teste comum de JVM o alcança.
 *
 * As duas consultas filtram por um dos identificadores sempre, e isso não é desempenho: é o que a
 * regra de `assignments` consegue autorizar. Uma consulta sem esse filtro pediria prescrições de
 * outras pessoas, e o Firestore recusa a consulta inteira em vez de devolver metade.
 */
internal class FirestoreAssignmentRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : AssignmentRepository {

    private val collection get() = firestore.collection(AssignmentDocument.COLLECTION)

    override suspend fun assignmentsOfProgram(trainerId: String, programId: String): List<Assignment> =
        withContext(dispatchers.io) {
            collection
                .whereEqualTo(AssignmentDocument.FIELD_TRAINER_ID, trainerId)
                .whereEqualTo(AssignmentDocument.FIELD_PROGRAM_ID, programId)
                .limit(AssignmentRepository.LIMIT)
                .get()
                .await()
                .documents
                .mapNotNull { AssignmentDocument.assignment(it.plainData()) }
        }

    override suspend fun activeAssignment(studentId: String): Assignment? = withContext(dispatchers.io) {
        collection
            .whereEqualTo(AssignmentDocument.FIELD_STUDENT_ID, studentId)
            .limit(AssignmentRepository.LIMIT)
            .get()
            .await()
            .documents
            .mapNotNull { AssignmentDocument.assignment(it.plainData()) }
            .firstOrNull { it.isActive }
    }

    /**
     * Grava por caminho exato, e não por id sorteado.
     *
     * `set` sem merge substitui o documento inteiro: é o que faz reatribuir sobrescrever a
     * prescrição anterior daquele par em vez de deixar campos velhos de um programa antigo
     * convivendo com os novos.
     */
    override suspend fun assign(assignment: Assignment): Assignment = withContext(dispatchers.io) {
        val fields = AssignmentDocument.toMap(assignment) +
            (AssignmentDocument.FIELD_UPDATED_AT to FieldValue.serverTimestamp())

        document(assignment).set(fields).await()
        assignment
    }

    override suspend fun end(assignment: Assignment) = withContext(dispatchers.io) {
        document(assignment)
            .update(
                mapOf(
                    AssignmentDocument.FIELD_STATUS to AssignmentStatus.ENDED.stored,
                    AssignmentDocument.FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                ),
            )
            .await()
        Unit
    }

    private fun document(assignment: Assignment) = collection.document(
        Assignment.id(trainerId = assignment.trainerId, studentId = assignment.studentId),
    )

    /**
     * O documento com o carimbo de tempo já em milissegundos.
     *
     * Existe porque `FieldValue.serverTimestamp()` volta como `Timestamp` do Firebase, e
     * [AssignmentDocument] não conhece — nem deve conhecer — tipo nenhum do SDK: é o que permite
     * afirmá-lo num teste comum de JVM. A conversão é a fronteira, e a fronteira é aqui.
     */
    private fun DocumentSnapshot.plainData(): Map<String, Any?>? = data?.plus(
        AssignmentDocument.FIELD_UPDATED_AT to getTimestamp(AssignmentDocument.FIELD_UPDATED_AT)?.toDate()?.time,
    )
}
