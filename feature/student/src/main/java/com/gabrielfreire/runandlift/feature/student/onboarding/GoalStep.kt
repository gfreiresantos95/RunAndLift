package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * Passo do objetivo.
 *
 * **Uma escolha só**, e é decisão: quem marca tudo não disse nada, e é o objetivo principal que
 * decide a estrutura do programa. Nuance é conversa para a avaliação com o treinador.
 *
 * Chips, como os outros passos. Aqui a troca vinda dos cartões não custou nada: os cinco objetivos
 * nunca tiveram frase de apoio — eles se explicam no rótulo —, então o cartão só gastava cinco
 * linhas de tela para dizer o que cabe em duas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GoalStep(selected: TrainingGoal?, onSelect: (TrainingGoal) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        TrainingGoal.entries.forEach { goal ->
            AppChoiceChip(
                label = goal.title(),
                selected = goal == selected,
                onClick = { onSelect(goal) },
            )
        }
    }
}

/** Os cinco objetivos com um marcado — e a quebra de linha, que é o que se confere em tela estreita. */
@LightDarkPreviews
@Composable
private fun GoalStepPreview() {
    RunAndLiftTheme {
        GoalStep(selected = TrainingGoal.HYPERTROPHY, onSelect = {})
    }
}
