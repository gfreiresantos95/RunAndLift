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
import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * Passo das modalidades de atendimento.
 *
 * **Não há opção "híbrido"**: híbrido é presencial e online marcados juntos. Uma terceira opção que
 * significa "as duas anteriores" é onde metade das pessoas marca as três, e a busca passa a ter
 * dois jeitos de dizer a mesma coisa.
 *
 * Três chips só — é a lista mais curta do fluxo, e é de propósito: esta é a pergunta que decide se
 * um aluno de outro estado sequer considera este treinador, e uma lista curta é respondida.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ServiceModesStep(
    selected: Set<ServiceMode>,
    onToggle: (ServiceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        ServiceMode.entries.forEach { mode ->
            AppChoiceChip(
                label = mode.label(),
                selected = mode in selected,
                onClick = { onToggle(mode) },
                multiSelect = true,
            )
        }
    }
}

/** Presencial e online juntos — que é como "híbrido" se diz aqui, e o que este preview mostra. */
@LightDarkPreviews
@Composable
private fun ServiceModesStepPreview() {
    RunAndLiftTheme {
        ServiceModesStep(selected = setOf(ServiceMode.IN_PERSON, ServiceMode.ONLINE), onToggle = {})
    }
}
