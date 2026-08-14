package com.gabrielfreire.runandlift.feature.student.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R
import java.time.DayOfWeek

/**
 * Dia da semana abreviado, em português.
 *
 * Vem de `R.string` e **não** de `DayOfWeek.getDisplayName`: aquele segue o idioma do aparelho, e
 * um app em português mostraria "Mon" para quem tem o celular em inglês. O nome do dia é conteúdo
 * da tela, não formatação de sistema.
 *
 * Abreviado porque são sete lado a lado — por extenso, "quarta-feira" não cabe em tela estreita
 * sem quebrar a linha dos outros seis.
 */
@Composable
internal fun DayOfWeek.shortLabel(): String = stringResource(
    when (this) {
        DayOfWeek.MONDAY -> R.string.student_day_monday
        DayOfWeek.TUESDAY -> R.string.student_day_tuesday
        DayOfWeek.WEDNESDAY -> R.string.student_day_wednesday
        DayOfWeek.THURSDAY -> R.string.student_day_thursday
        DayOfWeek.FRIDAY -> R.string.student_day_friday
        DayOfWeek.SATURDAY -> R.string.student_day_saturday
        DayOfWeek.SUNDAY -> R.string.student_day_sunday
    },
)

/** Nome por extenso, para o leitor de tela — a abreviação lida em voz alta vira sopa de letras. */
@Composable
internal fun DayOfWeek.fullLabel(): String = stringResource(
    when (this) {
        DayOfWeek.MONDAY -> R.string.student_day_monday_full
        DayOfWeek.TUESDAY -> R.string.student_day_tuesday_full
        DayOfWeek.WEDNESDAY -> R.string.student_day_wednesday_full
        DayOfWeek.THURSDAY -> R.string.student_day_thursday_full
        DayOfWeek.FRIDAY -> R.string.student_day_friday_full
        DayOfWeek.SATURDAY -> R.string.student_day_saturday_full
        DayOfWeek.SUNDAY -> R.string.student_day_sunday_full
    },
)
