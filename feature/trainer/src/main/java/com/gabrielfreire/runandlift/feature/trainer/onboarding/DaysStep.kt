package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.component.DayPicker
import java.time.DayOfWeek

/**
 * Passo dos dias de atendimento.
 *
 * A pergunta é sobre **disponibilidade**, e não sobre a agenda cheia: é o que um aluno consulta
 * antes de pedir vínculo, e o que evita o pedido que já nasce recusado. A agenda de verdade é outra
 * coisa, e vem com os horários marcados.
 *
 * A contagem embaixo existe para confirmar a escolha sem obrigar a recontar sete chips.
 */
@Composable
internal fun DaysStep(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        DayPicker(selected = selected, onToggle = onToggle)

        Text(
            text = if (selected.isEmpty()) {
                stringResource(R.string.trainer_onboarding_days_none)
            } else {
                pluralStringResource(R.plurals.trainer_onboarding_days_count, selected.size, selected.size)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@LightDarkPreviews
@Composable
private fun DaysStepPreview() {
    RunAndLiftTheme {
        DaysStep(selected = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), onToggle = {})
    }
}
