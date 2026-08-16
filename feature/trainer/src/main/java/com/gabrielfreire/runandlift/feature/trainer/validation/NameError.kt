package com.gabrielfreire.runandlift.feature.trainer.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/** O que pode estar errado no nome: não veio, ou veio pela metade. */
internal enum class NameError { REQUIRED, INCOMPLETE, }

@Composable
internal fun NameError.message(): String = stringResource(
    when (this) {
        NameError.REQUIRED -> R.string.trainer_error_name_required
        NameError.INCOMPLETE -> R.string.trainer_error_name_incomplete
    },
)
