package com.gabrielfreire.runandlift.feature.trainer.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O que pode estar errado no estado.
 *
 * Um caso só, e é a ausência: o valor vem de uma lista fechada do IBGE, então não há formato a
 * conferir — a mensagem manda **escolher**, e não corrigir.
 */
internal enum class StateError { REQUIRED, }

@Composable
internal fun StateError.message(): String = stringResource(
    when (this) {
        StateError.REQUIRED -> R.string.trainer_error_state_required
    },
)
