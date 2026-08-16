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
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * Passo das especialidades.
 *
 * **Escolha múltipla**, ao contrário do objetivo do aluno — e a assimetria é o conteúdo: o aluno
 * tem um objetivo principal que decide a estrutura do programa dele, e o treinador atende várias
 * frentes ao mesmo tempo. Obrigá-lo a escolher uma produziria um dado falso e o tiraria de buscas
 * que ele atenderia bem.
 *
 * As cinco primeiras são os cinco objetivos do aluno, palavra por palavra. É o que permite a busca
 * casar os dois lados sem tabela de tradução — ver `TrainerSpecialty`.
 *
 * Chips e não tela à parte: oito opções não precisam de busca, e mandar a pessoa para outra tela no
 * meio de um passo a passo cobra uma navegação por uma escolha que cabe aqui.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SpecialtiesStep(
    selected: Set<TrainerSpecialty>,
    onToggle: (TrainerSpecialty) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        TrainerSpecialty.entries.forEach { specialty ->
            AppChoiceChip(
                label = specialty.label(),
                selected = specialty in selected,
                onClick = { onToggle(specialty) },
                multiSelect = true,
            )
        }
    }
}

/** Com algumas marcadas: é o estado em que se confere a quebra de linha da lista mais longa do fluxo. */
@LightDarkPreviews
@Composable
private fun SpecialtiesStepPreview() {
    RunAndLiftTheme {
        SpecialtiesStep(
            selected = setOf(TrainerSpecialty.HYPERTROPHY, TrainerSpecialty.REHAB_SUPPORT),
            onToggle = {},
        )
    }
}
