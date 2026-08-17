package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * [ServiceMode] em palavras, pela mesma razão de [TrainerSpecialty.label] estar neste pacote.
 *
 * "Presencial" e não "na academia": o treinador que atende em estúdio, em condomínio ou ao ar livre
 * está no mesmo caso, e um rótulo que nomeia o lugar deixaria os três se perguntando se contam.
 */
@Composable
internal fun ServiceMode.label(): String = stringResource(
    when (this) {
        ServiceMode.IN_PERSON -> R.string.trainer_mode_in_person
        ServiceMode.ONLINE -> R.string.trainer_mode_online
        ServiceMode.HOME_VISIT -> R.string.trainer_mode_home_visit
    },
)
