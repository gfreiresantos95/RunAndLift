package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.Link
import com.google.firebase.firestore.DocumentSnapshot

/**
 * O documento do Firestore virando [Link].
 *
 * Arquivo próprio porque é o **outro sentido** de [LinkDocument]: lá está o que o app escreve, e os
 * mapas que ele monta são afirmados por teste comum; aqui está a ida ao `DocumentSnapshot`, que não
 * se constrói fora do Android.
 *
 * O que sobrou aqui é só isso: ler sete campos por nome. **A decisão — documento incompleto vira
 * ausência na lista, origem desconhecida vira convite, data ausente vira zero — está em
 * [LinkDocument.link]**, que recebe tipos comuns e tem teste próprio. Enquanto as duas coisas
 * moravam juntas, metade do arquivo era testável e a outra metade não, e a fronteira entre elas
 * ficava invisível.
 *
 * A data é o único campo que precisa de conversão, e ela acontece **aqui** pela mesma razão que em
 * `FirestoreProgramRepository`: `Timestamp` é tipo do SDK, e [LinkDocument] não conhece nenhum — é o
 * que permite afirmá-lo num teste comum de JVM.
 */
internal fun DocumentSnapshot.toLink(): Link? = LinkDocument.link(
    trainerId = getString(LinkDocument.FIELD_TRAINER_ID),
    studentId = getString(LinkDocument.FIELD_STUDENT_ID),
    status = getString(LinkDocument.FIELD_STATUS),
    origin = getString(LinkDocument.FIELD_ORIGIN),
    trainerName = getString(LinkDocument.FIELD_TRAINER_NAME),
    studentName = getString(LinkDocument.FIELD_STUDENT_NAME),
    createdAt = getTimestamp(LinkDocument.FIELD_CREATED_AT)?.toDate()?.time,
)
