package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.component.OptionCard
import com.gabrielfreire.runandlift.feature.student.text.description
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * Passo do nível de experiência.
 *
 * As três faixas ficam **todas visíveis**, e não atrás de um seletor: a escolha é entre elas, e
 * comparar as descrições é o que faz alguém acertar a própria faixa.
 */
@Composable
internal fun LevelStep(selected: TrainingLevel?, onSelect: (TrainingLevel) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        TrainingLevel.entries.forEach { level ->
            OptionCard(
                title = level.title(),
                description = level.description(),
                selected = level == selected,
                onSelect = { onSelect(level) },
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun LevelStepPreview() {
    RunAndLiftTheme {
        LevelStep(selected = TrainingLevel.INTERMEDIATE, onSelect = {})
    }
}
