package com.gabrielfreire.runandlift.feature.trainer.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/** O que pode estar errado na cidade. Ver [StateError] — mesma régua, outro campo. */
internal enum class CityError { REQUIRED, }

@Composable
internal fun CityError.message(): String = stringResource(
    when (this) {
        CityError.REQUIRED -> R.string.trainer_error_city_required
    },
)
