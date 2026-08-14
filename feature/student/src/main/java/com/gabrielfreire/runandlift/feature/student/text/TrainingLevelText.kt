package com.gabrielfreire.runandlift.feature.student.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.R

/**
 * [TrainingLevel] em palavras.
 *
 * Mora aqui, e não junto do enum, pela exceção que o projeto já abre para os enums de `:data`:
 * aquele módulo não tem recurso de string nem Compose, e este é o pacote mais próximo que pode
 * conhecer `R.string`.
 *
 * Cada faixa tem **título e descrição**: "intermediário" não quer dizer o mesmo para duas pessoas,
 * e é a frase de baixo que faz a escolha ser sobre o que se fez, não sobre como a pessoa se avalia.
 */
@Composable
internal fun TrainingLevel.title(): String = stringResource(
    when (this) {
        TrainingLevel.BEGINNER -> R.string.student_level_beginner
        TrainingLevel.INTERMEDIATE -> R.string.student_level_intermediate
        TrainingLevel.ADVANCED -> R.string.student_level_advanced
    },
)

@Composable
internal fun TrainingLevel.description(): String = stringResource(
    when (this) {
        TrainingLevel.BEGINNER -> R.string.student_level_beginner_description
        TrainingLevel.INTERMEDIATE -> R.string.student_level_intermediate_description
        TrainingLevel.ADVANCED -> R.string.student_level_advanced_description
    },
)
