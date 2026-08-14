package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode estar errado no celular do aluno.
 *
 * Um caso só: o campo é opcional para quem é aluno, então "vazio" não é erro — o que sobra é o
 * número pela metade.
 */
internal enum class PhoneError { INVALID, }

@Composable
internal fun PhoneError.message(): String = stringResource(
    when (this) {
        PhoneError.INVALID -> R.string.student_error_phone_invalid
    },
)
