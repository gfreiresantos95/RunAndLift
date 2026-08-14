package com.gabrielfreire.runandlift.feature.student.onboarding

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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.component.DayPicker
import java.time.DayOfWeek

/**
 * Passo dos dias disponíveis.
 *
 * A pergunta é sobre **poder**, e não sobre costume: o programa precisa saber a restrição de agenda,
 * e a frequência real vem da execução. O texto de apoio diz isso, porque "quais dias você treina" e
 * "quais dias você pode treinar" produzem respostas diferentes.
 *
 * A contagem embaixo existe para confirmar a escolha sem obrigar a recontar sete quadradinhos.
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
                stringResource(R.string.student_onboarding_days_none)
            } else {
                pluralStringResource(R.plurals.student_onboarding_days_count, selected.size, selected.size)
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
