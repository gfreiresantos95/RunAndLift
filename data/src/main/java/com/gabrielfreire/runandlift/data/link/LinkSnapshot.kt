package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.google.firebase.firestore.DocumentSnapshot

/**
 * O documento do Firestore virando [Link].
 *
 * Arquivo próprio porque é o **outro sentido** de [LinkDocument]: lá está o que o app escreve, e o
 * mapa que ele monta é afirmado por teste comum; aqui está o que ele lê, e isso depende de um
 * `DocumentSnapshot`, que não se constrói fora do Android. Juntos no mesmo arquivo, metade dele
 * seria testável e a outra metade não — e a fronteira entre as duas ficaria invisível.
 *
 * **Documento com campo faltando ou estranho vira ausência na lista, e não exceção.** Um documento
 * escrito por uma versão futura não pode derrubar a carteira de quem está tentando trabalhar agora.
 *
 * A origem é a exceção da exceção: ausente, o vínculo entra assim mesmo como convite. Perder um
 * aluno da lista porque o campo que diz **de onde ele veio** não foi escrito seria descartar um dado
 * central por causa de um estatístico.
 */
internal fun DocumentSnapshot.toLink(): Link? {
    val trainerId = getString(LinkDocument.FIELD_TRAINER_ID)
    val studentId = getString(LinkDocument.FIELD_STUDENT_ID)
    val status = LinkStatus.fromStored(getString(LinkDocument.FIELD_STATUS))

    if (trainerId == null || studentId == null || status == null) return null

    return Link(
        trainerId = trainerId,
        studentId = studentId,
        status = status,
        origin = LinkOrigin.fromStored(getString(LinkDocument.FIELD_ORIGIN)) ?: LinkOrigin.INVITE_CODE,
        trainerName = getString(LinkDocument.FIELD_TRAINER_NAME).orEmpty(),
        studentName = getString(LinkDocument.FIELD_STUDENT_NAME).orEmpty(),
    )
}
