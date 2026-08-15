package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.text.description
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * Passo do nível de experiência.
 *
 * As três faixas são **chips**, como as regiões de lesão e os dias — uma linguagem só para tudo o
 * que se escolhe neste passo a passo.
 *
 * **A descrição da faixa escolhida aparece abaixo**, e não some junto com os cartões. Ela é a razão
 * de a pergunta funcionar: autoavaliação de nível sem âncora é chute, e quase todo mundo se
 * superestima. "Treino há anos, sem parar por muito tempo, e acompanho minhas cargas" é o que faz
 * alguém reconhecer que não é avançado.
 *
 * O que se perde em relação aos cartões antigos é poder **comparar as três descrições de uma vez**;
 * o que se ganha é a lista caber num relance em vez de ocupar a tela inteira. Tocar em cada chip
 * troca a descrição na hora, então a comparação continua possível — custa um toque em vez de zero.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LevelStep(selected: TrainingLevel?, onSelect: (TrainingLevel) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            TrainingLevel.entries.forEach { level ->
                AppChoiceChip(
                    label = level.title(),
                    selected = level == selected,
                    onClick = { onSelect(level) },
                )
            }
        }

        selected?.let { level ->
            Text(
                text = level.description(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Com uma faixa escolhida: é o estado em que a descrição aparece, e o que se confere aqui. */
@LightDarkPreviews
@Composable
private fun LevelStepPreview() {
    RunAndLiftTheme {
        LevelStep(selected = TrainingLevel.INTERMEDIATE, onSelect = {})
    }
}

/** Sem escolha: os três chips e nenhuma descrição — o primeiro estado que a pessoa vê. */
@LightDarkPreviews
@Composable
private fun LevelStepEmptyPreview() {
    RunAndLiftTheme {
        LevelStep(selected = null, onSelect = {})
    }
}
