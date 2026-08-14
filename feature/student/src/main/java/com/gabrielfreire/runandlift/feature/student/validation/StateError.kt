package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode faltar no estado.
 *
 * Um caso só, e é o suficiente: o valor não é digitado, vem de uma lista fechada vinda do IBGE. Não
 * existe "estado inválido" para conferir — o que pode acontecer é a pessoa salvar sem ter escolhido.
 */
internal enum class StateError { REQUIRED, }

@Composable
internal fun StateError.message(): String = stringResource(
    when (this) {
        StateError.REQUIRED -> R.string.student_error_state_required
    },
)
