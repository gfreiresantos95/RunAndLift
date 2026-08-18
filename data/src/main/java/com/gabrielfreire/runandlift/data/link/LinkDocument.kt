package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.google.firebase.firestore.FieldValue

/**
 * Como o vínculo vira documento — o id, os nomes dos campos e os mapas de gravação.
 *
 * Mora fora de [FirestoreLinkRepository] pelo mesmo motivo de `TrainerDocument`: **aqui está a regra
 * que as Security Rules dependem**, e ali estão as chamadas ao SDK. Separadas, a regra pode ser
 * afirmada por um teste comum, sem emulador — e esta é a regra que, quebrada, não dá erro nenhum:
 * grava um documento perfeitamente válido que nenhuma regra consegue encontrar depois.
 *
 * A **decisão** da leitura também mora aqui, em [link], recebendo `String?` em vez de
 * `DocumentSnapshot` — que é tipo do Firebase e não se constrói num teste de JVM. Do outro lado da
 * fronteira, em `LinkSnapshot`, sobra só ler campo por nome, que é chamada ao SDK e não decisão.
 */
internal object LinkDocument {

    const val COLLECTION = "links"

    const val FIELD_TRAINER_ID = "trainerId"
    const val FIELD_STUDENT_ID = "studentId"
    const val FIELD_STATUS = "status"
    const val FIELD_ORIGIN = "origin"
    const val FIELD_TRAINER_NAME = "trainerName"
    const val FIELD_STUDENT_NAME = "studentName"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_UPDATED_AT = "updatedAt"

    /**
     * O id determinístico do vínculo: `{trainerId}_{studentId}`.
     *
     * É a convenção do ADR-0007, e a única forma de uma Security Rule descobrir se duas pessoas têm
     * vínculo: regra não consulta coleção, só faz `get()` num caminho que ela mesma consegue montar.
     * O separador é `_` porque `/` criaria subcoleção e `-` aparece dentro de uid gerado pelo
     * Firebase — o `_` não.
     *
     * A ordem também é regra: treinador primeiro. Invertida, o id existe, o documento grava e
     * `linkId(trainerId, studentId)` da regra aponta para o vazio.
     */
    fun id(trainerId: String, studentId: String): String = "${trainerId}_$studentId"

    /**
     * O mapa que cria o vínculo.
     *
     * [Link.trainerId] e [Link.studentId] vão como campos **além** de estarem no id: a regra lê os
     * dois para responder quem pode ler e quem pode confirmar, e `resource.id` não se parte em rule
     * sem `split()`, que não existe lá.
     *
     * `createdAt` e `updatedAt` são carimbados pelo servidor e ninguém os lê hoje. Vão assim mesmo,
     * porque são o tipo de dado que não se recupera depois: no dia em que a carteira mostrar "aluno
     * desde março", a resposta já vai estar gravada em vez de começar a ser coletada.
     */
    fun fields(link: Link): Map<String, Any> = mapOf(
        FIELD_TRAINER_ID to link.trainerId,
        FIELD_STUDENT_ID to link.studentId,
        FIELD_STATUS to link.status.stored,
        FIELD_ORIGIN to link.origin.stored,
        FIELD_TRAINER_NAME to link.trainerName,
        FIELD_STUDENT_NAME to link.studentName,
        FIELD_CREATED_AT to FieldValue.serverTimestamp(),
        FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
    )

    /**
     * O mapa que reabre um vínculo encerrado.
     *
     * Reabrir é escrever no **mesmo documento**: o id é determinístico, e não existe segundo
     * documento possível para o mesmo par de pessoas. Por isso os nomes vão junto — é a chance de
     * corrigir quem se chamava de outro jeito quando o vínculo terminou.
     *
     * `createdAt` fica de fora de propósito: a data em que essas duas pessoas se encontraram pela
     * primeira vez não muda porque a relação foi retomada.
     */
    fun renewFields(link: Link): Map<String, Any> = mapOf(
        FIELD_STATUS to link.status.stored,
        FIELD_ORIGIN to link.origin.stored,
        FIELD_TRAINER_NAME to link.trainerName,
        FIELD_STUDENT_NAME to link.studentName,
        FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
    )

    /**
     * O caminho de volta: os campos gravados virando [Link], ou `null` quando não dá.
     *
     * **Documento com campo faltando ou estranho vira ausência na lista, e não exceção.** Um
     * documento escrito por uma versão futura não pode derrubar a carteira de quem está tentando
     * trabalhar agora — e derrubaria a lista inteira, não só aquela linha.
     *
     * A origem é a exceção da exceção: ausente ou desconhecida, o vínculo entra assim mesmo como
     * convite. Perder um aluno da lista porque o campo que diz **de onde ele veio** não foi escrito
     * seria descartar um dado central por causa de um estatístico.
     *
     * Recebe `String?` em vez de `DocumentSnapshot` de propósito: assim a decisão fica deste lado da
     * fronteira, onde um teste comum a alcança, e do outro lado sobra só a leitura dos campos.
     */
    fun link(
        trainerId: String?,
        studentId: String?,
        status: String?,
        origin: String? = null,
        trainerName: String? = null,
        studentName: String? = null,
    ): Link? {
        val parsedStatus = LinkStatus.fromStored(status)

        if (trainerId == null || studentId == null || parsedStatus == null) return null

        return Link(
            trainerId = trainerId,
            studentId = studentId,
            status = parsedStatus,
            origin = LinkOrigin.fromStored(origin) ?: LinkOrigin.INVITE_CODE,
            trainerName = trainerName.orEmpty(),
            studentName = studentName.orEmpty(),
        )
    }

    /**
     * O mapa que muda o estado, e **só** o estado.
     *
     * Nem os dois identificadores nem os nomes entram: a regra de `update` exige que treinador e
     * aluno cheguem iguais aos gravados, e reenviá-los é reenviar a chance de mandar diferente.
     * Aceitar, pausar e encerrar passam todos por aqui.
     */
    fun statusFields(status: LinkStatus): Map<String, Any> = mapOf(
        FIELD_STATUS to status.stored,
        FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
    )
}
