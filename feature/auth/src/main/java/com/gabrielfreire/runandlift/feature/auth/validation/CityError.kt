package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar na cidade.
 *
 * Um caso só, pela mesma razão de [StateError]: o valor vem de uma lista fechada, e a única falha
 * possível é não ter escolhido.
 *
 * Não existe caso "escolha o estado antes". A tela resolve isso desabilitando o campo até haver um
 * estado — impedir é melhor que acusar, e um erro para uma situação que a interface não deixa
 * acontecer é uma frase que ninguém lê.
 */
internal enum class CityError { REQUIRED, }

@Composable
internal fun CityError.message(): String = stringResource(
    when (this) {
        CityError.REQUIRED -> R.string.auth_error_city_required
    },
)
