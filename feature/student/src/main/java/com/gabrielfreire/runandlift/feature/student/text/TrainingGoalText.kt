package com.gabrielfreire.runandlift.feature.student.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.student.R

/**
 * [TrainingGoal] em palavras, pela mesma razão de [TrainingLevel.title] estar neste pacote.
 *
 * Sem descrição de apoio: os cinco objetivos se explicam sozinhos no rótulo, e uma frase abaixo de
 * cada um faria a lista virar um texto que ninguém lê para escolher uma coisa óbvia.
 */
@Composable
internal fun TrainingGoal.title(): String = stringResource(
    when (this) {
        TrainingGoal.HYPERTROPHY -> R.string.student_goal_hypertrophy
        TrainingGoal.STRENGTH -> R.string.student_goal_strength
        TrainingGoal.WEIGHT_LOSS -> R.string.student_goal_weight_loss
        TrainingGoal.CONDITIONING -> R.string.student_goal_conditioning
        TrainingGoal.HEALTH -> R.string.student_goal_health
    },
)
