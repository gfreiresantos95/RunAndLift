package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * [LinkStatus] em palavras, **ditas do lado do treinador**.
 *
 * É por isso que este mapeamento não pode morar em `:data` junto do enum, e nem ser compartilhado
 * com o módulo do aluno: o mesmo `REQUESTED` é "pediu para treinar com você" aqui e "aguardando
 * resposta" lá. Um texto só para os dois teria de ser genérico o bastante para não dizer nada.
 */
@Composable
internal fun LinkStatus.label(): String = stringResource(
    when (this) {
        LinkStatus.INVITED -> R.string.trainer_link_invited
        LinkStatus.REQUESTED -> R.string.trainer_link_requested
        LinkStatus.ACTIVE -> R.string.trainer_link_active
        LinkStatus.PAUSED -> R.string.trainer_link_paused
        LinkStatus.ENDED -> R.string.trainer_link_ended
    },
)
