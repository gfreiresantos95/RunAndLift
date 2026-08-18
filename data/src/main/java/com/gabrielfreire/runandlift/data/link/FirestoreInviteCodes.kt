package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.trainer.TrainerDocument
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/**
 * A coleção `inviteCodes` e o campo do perfil que aponta para ela.
 *
 * Existe separado de [FirestoreLinkRepository] porque são **dois assuntos com regras diferentes de
 * leitura**: vínculo se consulta por identificador e vale para sempre; convite se lê por caminho
 * exato, é do treinador e é substituível. Juntos, davam uma classe que abria três coleções e cuidava
 * de duas vidas úteis distintas.
 *
 * O código vive em dois lugares de propósito: o documento `inviteCodes/{code}` é o que o **aluno**
 * lê para descobrir de quem é o código, e o campo em `trainerProfiles/{uid}` é o que o **treinador**
 * lê para descobrir qual é o dele — sem esse segundo, achar o próprio código exigiria consultar uma
 * coleção que qualquer autenticado lê.
 */
internal class FirestoreInviteCodes(private val firestore: FirebaseFirestore) {

    /** Cache primeiro: é o documento do próprio titular, e ele muda raramente. */
    suspend fun code(trainerId: String): String? = readProfile(trainerId)
        .getString(TrainerDocument.FIELD_INVITE_CODE)

    /**
     * Cria um código e descarta o anterior, no mesmo lote.
     *
     * Meio convite deixaria um código sem dono ou um dono apontando para um convite que não existe —
     * e, no meio errado, dois códigos valendo para o mesmo treinador.
     */
    suspend fun create(trainerId: String, trainerName: String): String {
        val previous = code(trainerId)
        val code = freeCode()

        firestore.batch()
            .set(inviteDocument(code), InviteCodeDocument.fields(trainerId, trainerName))
            .set(
                trainerDocument(trainerId),
                mapOf(TrainerDocument.FIELD_INVITE_CODE to code),
                SetOptions.merge(),
            )
            .apply { if (previous != null && previous != code) delete(inviteDocument(previous)) }
            .commit()
            .await()

        return code
    }

    /**
     * De quem é o código digitado, ou `null` se não existir nenhum com ele.
     *
     * Servidor, e não cache: um código acabou de ser criado do outro lado, e não há nada em disco
     * sobre ele. Ler do cache aqui seria responder "não existe" para todo código novo.
     */
    suspend fun find(code: String): InviteCode? {
        val normalized = InviteCodeDocument.normalize(code)

        if (normalized.length != InviteCodeDocument.LENGTH) return null

        val document = inviteDocument(normalized).get(Source.SERVER).await()
        val trainerId = document.getString(InviteCodeDocument.FIELD_TRAINER_ID)

        return trainerId?.let {
            InviteCode(
                code = normalized,
                trainerId = it,
                trainerName = document.getString(InviteCodeDocument.FIELD_TRAINER_NAME).orEmpty(),
            )
        }
    }

    /**
     * Um código que ainda não está em uso.
     *
     * A colisão é improvável — são mais de um bilhão de combinações —, mas o estrago dela seria dar a
     * um treinador o convite de outro, e conferir custa uma leitura numa operação que acontece uma
     * vez por treinador. Depois de [ATTEMPTS] tentativas o sorteio segue mesmo assim: insistir num
     * laço sem fim travaria a tela por causa de um azar que não existe.
     *
     * Leitura que falha conta como código livre — sem rede, quem recusa é a gravação seguinte, e com
     * uma mensagem melhor do que "não consegui sortear um código".
     */
    private suspend fun freeCode(): String {
        repeat(ATTEMPTS) {
            val code = InviteCodeDocument.newCode()
            val taken = runCatching { inviteDocument(code).get(Source.SERVER).await().exists() }

            if (!taken.getOrDefault(false)) return code
        }

        return InviteCodeDocument.newCode()
    }

    private suspend fun readProfile(trainerId: String): DocumentSnapshot =
        runCatching { trainerDocument(trainerId).get(Source.CACHE).await() }
            .getOrNull()
            ?.takeIf { it.exists() }
            ?: trainerDocument(trainerId).get(Source.SERVER).await()

    private fun inviteDocument(code: String) = firestore.collection(InviteCodeDocument.COLLECTION).document(code)

    private fun trainerDocument(uid: String) = firestore.collection(TrainerDocument.COLLECTION).document(uid)

    private companion object {

        const val ATTEMPTS = 3
    }
}
