package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * [TrainerSpecialty] em palavras, pela mesma razão de [TrainerExperience.title] estar neste pacote.
 *
 * As cinco primeiras são **as mesmas frases** que o aluno lê ao escolher o objetivo dele, e isso é
 * proposital: o treinador precisa reconhecer, na hora de marcar, o texto exato que vai ser
 * comparado com o do aluno. Duas grafias da mesma coisa fariam os dois lados parecerem falar de
 * coisas diferentes.
 */
@Composable
internal fun TrainerSpecialty.label(): String = stringResource(
    when (this) {
        TrainerSpecialty.HYPERTROPHY -> R.string.trainer_specialty_hypertrophy
        TrainerSpecialty.STRENGTH -> R.string.trainer_specialty_strength
        TrainerSpecialty.WEIGHT_LOSS -> R.string.trainer_specialty_weight_loss
        TrainerSpecialty.CONDITIONING -> R.string.trainer_specialty_conditioning
        TrainerSpecialty.HEALTH -> R.string.trainer_specialty_health
        TrainerSpecialty.RUNNING -> R.string.trainer_specialty_running
        TrainerSpecialty.REHAB_SUPPORT -> R.string.trainer_specialty_rehab
        TrainerSpecialty.SENIORS -> R.string.trainer_specialty_seniors
    },
)
