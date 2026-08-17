package com.gabrielfreire.runandlift.feature.trainer.onboarding

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
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.feature.trainer.text.title

/**
 * Passo do tempo de atuação.
 *
 * **Uma escolha só**, em faixas: é o que um aluno consegue interpretar de relance ao comparar dois
 * perfis. Um campo de anos exigiria manutenção anual do próprio dono para continuar verdadeiro.
 *
 * Sem frase de apoio abaixo da escolha, ao contrário do nível do aluno: "de dois a cinco anos" já
 * quer dizer a mesma coisa para todo mundo, enquanto "intermediário" não.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExperienceStep(
    selected: TrainerExperience?,
    onSelect: (TrainerExperience) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        TrainerExperience.entries.forEach { experience ->
            AppChoiceChip(
                label = experience.title(),
                selected = experience == selected,
                onClick = { onSelect(experience) },
            )
        }
    }
}

/** As quatro faixas com uma marcada — e a quebra de linha, que é o que se confere em tela estreita. */
@LightDarkPreviews
@Composable
private fun ExperienceStepPreview() {
    RunAndLiftTheme {
        ExperienceStep(selected = TrainerExperience.TWO_TO_FIVE_YEARS, onSelect = {})
    }
}
