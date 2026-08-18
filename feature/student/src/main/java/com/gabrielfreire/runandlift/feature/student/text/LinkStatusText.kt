package com.gabrielfreire.runandlift.feature.student.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.student.R

/**
 * [LinkStatus] em palavras, **ditas do lado do aluno**.
 *
 * O módulo do treinador tem o seu próprio, e não é duplicação por descuido: o mesmo `REQUESTED` é
 * "pediu para treinar com você" lá e "aguardando a resposta dele" aqui. Um texto só para os dois
 * teria de ser genérico o bastante para não dizer nada a nenhum dos lados — e os dois módulos não se
 * enxergam, que é a fronteira funcionando.
 */
@Composable
internal fun LinkStatus.label(): String = stringResource(
    when (this) {
        LinkStatus.INVITED -> R.string.student_link_invited
        LinkStatus.REQUESTED -> R.string.student_link_requested
        LinkStatus.ACTIVE -> R.string.student_link_active
        LinkStatus.PAUSED -> R.string.student_link_paused
        LinkStatus.ENDED -> R.string.student_link_ended
    },
)
