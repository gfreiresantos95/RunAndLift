package com.gabrielfreire.runandlift.feature.trainer.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * [TrainerExperience] em palavras.
 *
 * Mora aqui, e não junto do enum, pela exceção que o projeto já abre para os enums de `:data`:
 * aquele módulo não tem recurso de string nem Compose, e este é o pacote mais próximo que pode
 * conhecer `R.string`.
 *
 * Só título, sem descrição de apoio: "de dois a cinco anos" não quer dizer duas coisas para duas
 * pessoas, ao contrário de "intermediário" do lado do aluno — que é justamente por isso que lá há
 * uma frase embaixo e aqui não.
 */
@Composable
internal fun TrainerExperience.title(): String = stringResource(
    when (this) {
        TrainerExperience.UP_TO_TWO_YEARS -> R.string.trainer_experience_up_to_two
        TrainerExperience.TWO_TO_FIVE_YEARS -> R.string.trainer_experience_two_to_five
        TrainerExperience.FIVE_TO_TEN_YEARS -> R.string.trainer_experience_five_to_ten
        TrainerExperience.OVER_TEN_YEARS -> R.string.trainer_experience_over_ten
    },
)
