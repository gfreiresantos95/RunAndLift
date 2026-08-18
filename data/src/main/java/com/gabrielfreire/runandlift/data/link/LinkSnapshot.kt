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
 * O que sobrou aqui é só isso: ler seis campos por nome. **A decisão — documento incompleto vira
 * ausência na lista, origem desconhecida vira convite — está em [LinkDocument.link]**, que recebe
 * `String?` e tem teste próprio. Enquanto as duas coisas moravam juntas, metade do arquivo era
 * testável e a outra metade não, e a fronteira entre elas ficava invisível.
 */
internal fun DocumentSnapshot.toLink(): Link? = LinkDocument.link(
    trainerId = getString(LinkDocument.FIELD_TRAINER_ID),
    studentId = getString(LinkDocument.FIELD_STUDENT_ID),
    status = getString(LinkDocument.FIELD_STATUS),
    origin = getString(LinkDocument.FIELD_ORIGIN),
    trainerName = getString(LinkDocument.FIELD_TRAINER_NAME),
    studentName = getString(LinkDocument.FIELD_STUDENT_NAME),
)
