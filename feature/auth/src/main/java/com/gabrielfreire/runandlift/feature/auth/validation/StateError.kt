package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no estado.
 *
 * Um caso só, e é o suficiente: o valor não é digitado, vem de uma lista fechada. Não existe "estado
 * inválido" para conferir aqui — quem garante que a sigla existe é a própria lista, que veio do
 * IBGE. O que pode acontecer é a pessoa enviar sem ter escolhido.
 */
internal enum class StateError { REQUIRED, }

@Composable
internal fun StateError.message(): String = stringResource(
    when (this) {
        StateError.REQUIRED -> R.string.auth_error_state_required
    },
)
