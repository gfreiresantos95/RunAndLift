package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode estar errado no nome.
 *
 * Vazio e incompleto são casos separados porque as frases são diferentes: quem não escreveu nada
 * precisa saber que o campo é obrigatório, e quem escreveu "Ana" precisa saber que falta o
 * sobrenome — não que errou.
 */
internal enum class NameError { REQUIRED, INCOMPLETE }

@Composable
internal fun NameError.message(): String = stringResource(
    when (this) {
        NameError.REQUIRED -> R.string.student_error_name_required
        NameError.INCOMPLETE -> R.string.student_error_name_incomplete
    },
)
