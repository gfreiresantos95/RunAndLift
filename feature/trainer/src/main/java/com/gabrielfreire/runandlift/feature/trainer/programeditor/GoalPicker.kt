package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * O objetivo do programa, em chips.
 *
 * São os **mesmos cinco valores que o aluno escolhe no perfil**, e é essa coincidência que dá
 * sentido ao campo: ela é o que vai permitir, mais adiante, casar um programa de hipertrofia com
 * quem pediu hipertrofia sem tabela de tradução no meio.
 *
 * Escolha única, e desmarcável tocando de novo — sem essa saída, um objetivo marcado por engano no
 * primeiro toque não teria como voltar a ser nenhum, que é o estado inicial legítimo de um rascunho.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GoalPicker(selected: TrainingGoal?, onSelect: (TrainingGoal) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        Text(
            text = stringResource(R.string.trainer_program_goal),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            TrainingGoal.entries.forEach { goal ->
                AppChoiceChip(
                    label = goal.label(),
                    selected = goal == selected,
                    onClick = { onSelect(goal) },
                )
            }
        }
    }
}

/** Sem escolha nenhuma, que é como um programa novo abre — e o estado que um exemplo feliz esconde. */
@LightDarkPreviews
@Composable
private fun GoalPickerPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                GoalPicker(selected = null, onSelect = {})
                GoalPicker(selected = TrainingGoal.HYPERTROPHY, onSelect = {})
            }
        }
    }
}
