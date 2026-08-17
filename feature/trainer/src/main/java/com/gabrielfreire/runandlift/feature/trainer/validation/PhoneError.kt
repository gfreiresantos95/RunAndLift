package com.gabrielfreire.runandlift.feature.trainer.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O que pode estar errado no celular do treinador.
 *
 * **Dois casos, e não um como no aluno**: aqui o campo é obrigatório, então "vazio" é erro por si
 * só. A frase de cada caso é diferente porque a correção é diferente — um pede um número, o outro
 * pede que se confira o que já está lá.
 */
internal enum class PhoneError { REQUIRED, INVALID, }

@Composable
internal fun PhoneError.message(): String = stringResource(
    when (this) {
        PhoneError.REQUIRED -> R.string.trainer_error_phone_required
        PhoneError.INVALID -> R.string.trainer_error_phone_invalid
    },
)
