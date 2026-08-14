package com.gabrielfreire.runandlift.feature.student.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode faltar na cidade.
 *
 * Um caso só, pela mesma razão de [StateError]. Não existe "escolha o estado antes": a tela
 * desabilita o campo até haver um estado, e impedir é melhor que acusar.
 */
internal enum class CityError { REQUIRED, }

@Composable
internal fun CityError.message(): String = stringResource(
    when (this) {
        CityError.REQUIRED -> R.string.student_error_city_required
    },
)
