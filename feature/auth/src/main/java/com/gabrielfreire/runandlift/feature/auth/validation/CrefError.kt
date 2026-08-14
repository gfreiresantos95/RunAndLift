package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no registro no CREF.
 *
 * A máscara `######-A/AA` barra o formato na digitação — dígito onde é dígito, letra onde é letra.
 * O que sobra é o que ela não tem como saber: se está completo, se a **categoria** é uma das que
 * prescrevem, e se a sigla é de um estado que existe.
 *
 * [INVALID_CATEGORY] é caso próprio, e não mais um "inválido": errar a categoria é o único desses
 * erros em que a pessoa digitou algo plausível e precisa saber **qual** letra o app espera. Dizer
 * só "registro inválido" para quem escreveu `012345E/SP` a manda conferir o número, que está certo.
 */
internal enum class CrefError { REQUIRED, INVALID, INVALID_CATEGORY }

/**
 * [CrefError.INVALID] aponta as três partes e repete o exemplo: "inválido" sozinho deixa a pessoa
 * adivinhando qual delas errou, num campo que ela copia de uma carteira. [CrefError.INVALID_CATEGORY]
 * é mais específico porque pode ser: as letras aceitas são duas, e cabem na frase.
 */
@Composable
internal fun CrefError.message(): String = stringResource(
    when (this) {
        CrefError.REQUIRED -> R.string.auth_error_cref_required
        CrefError.INVALID -> R.string.auth_error_cref_invalid
        CrefError.INVALID_CATEGORY -> R.string.auth_error_cref_category
    },
)
