package com.gabrielfreire.runandlift.feature.student.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.rememberSelectionHaptics
import com.gabrielfreire.runandlift.feature.student.text.fullLabel
import com.gabrielfreire.runandlift.feature.student.text.shortLabel
import java.time.DayOfWeek

/**
 * Os sete dias da semana, ligando e desligando.
 *
 * Começa na **segunda-feira**, e não no domingo: é a semana de treino como as pessoas a organizam
 * aqui, e `DayOfWeek.entries` já vem nessa ordem.
 *
 * O que a tela mostra é a abreviação — sete rótulos por extenso não cabem lado a lado —, mas o que
 * o leitor de tela anuncia é o nome **completo** mais o estado. "Seg, selecionado" lido em voz alta
 * é sopa de letras; "segunda-feira, selecionado" é uma frase.
 *
 * `toggleable` com `Role.Checkbox` porque as escolhas são independentes: marcar terça não desmarca
 * quinta, e o anúncio precisa deixar isso claro desde o primeiro toque.
 */
@Composable
internal fun DayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        DayOfWeek.entries.forEach { day ->
            DayToggle(
                day = day,
                selected = day in selected,
                onToggle = { onToggle(day) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayToggle(day: DayOfWeek, selected: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val label = day.fullLabel()
    val haptics = rememberSelectionHaptics()

    Surface(
        modifier = modifier
            .height(Dimens.MinTouchTarget)
            // As sete teclas são pequenas e ficam coladas: aqui o retorno tátil não é enfeite, é o
            // que diz que o dedo pegou a certa sem obrigar a conferir com o olho.
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { value ->
                    haptics.toggled(value)
                    onToggle()
                },
            )
            // O rótulo curto é decoração para o olho; quem ouve recebe o nome inteiro, e o estado
            // vem do próprio `toggleable`.
            .clearAndSetSemantics { contentDescription = label },
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = day.shortLabel(), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Com alguns dias marcados, que é o estado em que se confere se a linha cabe em tela estreita. */
@LightDarkPreviews
@Composable
private fun DayPickerPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DayPicker(
                selected = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                onToggle = {},
            )
        }
    }
}
