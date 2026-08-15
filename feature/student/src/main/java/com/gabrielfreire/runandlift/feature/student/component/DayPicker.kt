package com.gabrielfreire.runandlift.feature.student.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.feature.student.text.fullLabel
import com.gabrielfreire.runandlift.feature.student.text.shortLabel
import java.time.DayOfWeek

/**
 * Os sete dias da semana, ligando e desligando.
 *
 * Começa na **segunda-feira**, e não no domingo: é a semana de treino como as pessoas a organizam
 * aqui, e `DayOfWeek.entries` já vem nessa ordem.
 *
 * São **chips**, como o nível, o objetivo e as lesões. Antes eram sete quadrados de largura igual
 * dividindo a linha, o que dava uma tira alinhada — e um terceiro código de cor num passo a passo
 * que já tinha dois. Em `FlowRow` eles quebram para a linha de baixo quando não couberem, que é o
 * que acontece com a fonte do sistema aumentada; a tira de largura fixa, nesse caso, espremia
 * "Qua" até cortar.
 *
 * O que a tela mostra é a abreviação — sete rótulos por extenso não cabem —, mas o que o leitor de
 * tela anuncia é o nome **completo** mais o estado. "Seg, selecionado" lido em voz alta é sopa de
 * letras; "segunda-feira, selecionado" é uma frase.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayPicker(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        DayOfWeek.entries.forEach { day ->
            AppChoiceChip(
                label = day.shortLabel(),
                selected = day in selected,
                onClick = { onToggle(day) },
                // Independentes: marcar terça não desmarca quinta, e o anúncio precisa deixar isso
                // claro desde o primeiro toque.
                multiSelect = true,
                contentDescription = day.fullLabel(),
            )
        }
    }
}

/** Com alguns dias marcados, que é o estado em que se confere se a fileira cabe em tela estreita. */
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
