package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * [TrainingGoal] em palavras, do lado do treinador.
 *
 * É a **terceira** cópia da mesma tradução no projeto — o aluno tem a dele, e as especialidades do
 * treinador repetem os cinco valores palavra por palavra. Continua duplicada de propósito: os
 * módulos de papel não se enxergam, e o único lar comum seria um módulo compartilhado entre
 * features, que é exatamente a dependência que a arquitetura recusa. Cinco linhas de texto custam
 * menos que essa seta.
 */
@Composable
internal fun TrainingGoal.label(): String = stringResource(
    when (this) {
        TrainingGoal.HYPERTROPHY -> R.string.trainer_goal_hypertrophy
        TrainingGoal.STRENGTH -> R.string.trainer_goal_strength
        TrainingGoal.WEIGHT_LOSS -> R.string.trainer_goal_weight_loss
        TrainingGoal.CONDITIONING -> R.string.trainer_goal_conditioning
        TrainingGoal.HEALTH -> R.string.trainer_goal_health
    },
)
