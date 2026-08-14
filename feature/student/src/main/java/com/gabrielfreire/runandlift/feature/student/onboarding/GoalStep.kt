package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.feature.student.component.OptionCard
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * Passo do objetivo.
 *
 * **Uma escolha só**, e é decisão: quem marca tudo não disse nada, e é o objetivo principal que
 * decide a estrutura do programa. Nuance é conversa para a avaliação com o treinador.
 */
@Composable
internal fun GoalStep(selected: TrainingGoal?, onSelect: (TrainingGoal) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        TrainingGoal.entries.forEach { goal ->
            OptionCard(
                title = goal.title(),
                selected = goal == selected,
                onSelect = { onSelect(goal) },
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun GoalStepPreview() {
    RunAndLiftTheme {
        GoalStep(selected = TrainingGoal.HYPERTROPHY, onSelect = {})
    }
}
