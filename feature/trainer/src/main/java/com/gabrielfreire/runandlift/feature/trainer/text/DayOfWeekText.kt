package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R
import java.time.DayOfWeek

/**
 * Dia da semana abreviado, em português.
 *
 * Vem de `R.string` e **não** de `DayOfWeek.getDisplayName`: aquele segue o idioma do aparelho, e
 * um app em português mostraria "Mon" para quem tem o celular em inglês. O nome do dia é conteúdo
 * da tela, não formatação de sistema.
 *
 * **É uma segunda cópia do que o `:feature:student` tem**, e é deliberado, pela mesma razão de
 * `MainDispatcherRule` e das validações: os dois módulos não se enxergam, e é isso que impede uma
 * tela de treinador de importar uma rota de aluno. Mover estas catorze palavras para o `:core`
 * colocaria idioma dentro do design system, que é justamente o que ele não tem.
 */
@Composable
internal fun DayOfWeek.shortLabel(): String = stringResource(
    when (this) {
        DayOfWeek.MONDAY -> R.string.trainer_day_monday
        DayOfWeek.TUESDAY -> R.string.trainer_day_tuesday
        DayOfWeek.WEDNESDAY -> R.string.trainer_day_wednesday
        DayOfWeek.THURSDAY -> R.string.trainer_day_thursday
        DayOfWeek.FRIDAY -> R.string.trainer_day_friday
        DayOfWeek.SATURDAY -> R.string.trainer_day_saturday
        DayOfWeek.SUNDAY -> R.string.trainer_day_sunday
    },
)

/** Nome por extenso, para o leitor de tela — a abreviação lida em voz alta vira sopa de letras. */
@Composable
internal fun DayOfWeek.fullLabel(): String = stringResource(
    when (this) {
        DayOfWeek.MONDAY -> R.string.trainer_day_monday_full
        DayOfWeek.TUESDAY -> R.string.trainer_day_tuesday_full
        DayOfWeek.WEDNESDAY -> R.string.trainer_day_wednesday_full
        DayOfWeek.THURSDAY -> R.string.trainer_day_thursday_full
        DayOfWeek.FRIDAY -> R.string.trainer_day_friday_full
        DayOfWeek.SATURDAY -> R.string.trainer_day_saturday_full
        DayOfWeek.SUNDAY -> R.string.trainer_day_sunday_full
    },
)
