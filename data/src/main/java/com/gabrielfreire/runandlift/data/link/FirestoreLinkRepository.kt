package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * [LinkRepository] sobre o Firestore.
 *
 * O que sobrou aqui é **conversa com o SDK** sobre a coleção `links`: consultar, ler snapshot,
 * escrever. O id do vínculo e os mapas de gravação moram em [LinkDocument], testáveis sem emulador,
 * e tudo o que é convite mora em [FirestoreInviteCodes] — outra coleção, outra vida útil.
 *
 * Este é o único repositório do projeto que **consulta uma coleção** em vez de ler documento por
 * caminho. A consulta é sempre por igualdade num dos dois identificadores, e isso não é só
 * desempenho: é o que a regra de `links` consegue autorizar. Uma consulta sem esse filtro pediria
 * documentos de outras pessoas, e o Firestore recusa a consulta inteira em vez de devolver metade.
 */
internal class FirestoreLinkRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
    private val invites: FirestoreInviteCodes = FirestoreInviteCodes(firestore),
) : LinkRepository {

    override suspend fun trainerLinks(trainerId: String): List<Link> = withContext(dispatchers.io) {
        linksWhere(field = LinkDocument.FIELD_TRAINER_ID, uid = trainerId)
    }

    override suspend fun studentLinks(studentId: String): List<Link> = withContext(dispatchers.io) {
        linksWhere(field = LinkDocument.FIELD_STUDENT_ID, uid = studentId)
    }

    override suspend fun inviteCode(trainerId: String): String? = withContext(dispatchers.io) {
        invites.code(trainerId)
    }

    override suspend fun createInviteCode(trainerId: String, trainerName: String): String =
        withContext(dispatchers.io) { invites.create(trainerId, trainerName) }

    override suspend fun findInvite(code: String): InviteCode? = withContext(dispatchers.io) {
        invites.find(code)
    }

    override suspend fun requestLink(
        invite: InviteCode,
        studentId: String,
        studentName: String,
        existing: Link?,
    ): LinkRequestResult = withContext(dispatchers.io) {
        // A regra mora em `LinkRequest`, onde um teste comum a alcança; o que sobra aqui é a
        // escrita que cada uma das três decisões pede.
        when (val request = LinkRequest.of(invite, studentId, studentName, existing)) {
            is LinkRequest.Rejected -> LinkRequestResult.Failure(request.reason)
            is LinkRequest.Create -> write(request.link) { set(LinkDocument.fields(request.link)) }
            is LinkRequest.Renew -> write(request.link) { update(LinkDocument.renewFields(request.link)) }
        }
    }

    override suspend fun updateStatus(link: Link, status: LinkStatus): Link = withContext(dispatchers.io) {
        linkDocument(link).update(LinkDocument.statusFields(status)).await()

        // Devolve o estado resultante em vez de reler: uma segunda leitura custaria do orçamento
        // para responder o que esta chamada acabou de escrever.
        link.copy(status = status)
    }

    /**
     * A escrita do pedido, com a falha virando resultado em vez de exceção.
     *
     * Uma função para criar e para reabrir porque a diferença entre as duas é só o mapa enviado — e
     * a recusa da regra, que é o que mais acontece aqui, precisa chegar à tela como uma frase.
     */
    private suspend fun write(link: Link, block: DocumentReference.() -> Task<Void>): LinkRequestResult =
        runCatching { linkDocument(link).block().await() }.fold(
            onSuccess = { LinkRequestResult.Success(link) },
            onFailure = { LinkRequestResult.Failure(LinkRequestFailure.UNKNOWN, it) },
        )

    private suspend fun linksWhere(field: String, uid: String): List<Link> = firestore
        .collection(LinkDocument.COLLECTION)
        .whereEqualTo(field, uid)
        .limit(LinkRepository.LIMIT)
        .get()
        .await()
        .documents
        .mapNotNull { it.toLink() }

    private fun linkDocument(link: Link) = firestore
        .collection(LinkDocument.COLLECTION)
        .document(LinkDocument.id(trainerId = link.trainerId, studentId = link.studentId))
}
