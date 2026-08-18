package com.gabrielfreire.runandlift.feature.student.trainer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.data.link.LinkRequestFailure
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O que pode dar errado entre digitar um código e ter um pedido feito.
 *
 * São quatro porque são quatro conversas diferentes, e três delas não pedem "tente de novo": código
 * inexistente pede conferir o que foi digitado, código próprio pede entender o que aconteceu, e
 * vínculo já existente não é erro nenhum — é a resposta certa para quem pediu duas vezes.
 *
 * Existe aqui, e não em `:data`, porque junta a falha do pedido ([LinkRequestFailure]) com a única
 * que acontece antes dele: o código não existir. Para quem digitou, as duas coisas são o mesmo
 * momento.
 */
internal enum class TrainerCodeError {

    /** Não existe convite com esse código. */
    NOT_FOUND,

    /** O código é do próprio usuário, que é treinador e aluno na mesma conta. */
    OWN_CODE,

    /** Já existe vínculo com esse treinador. */
    ALREADY_LINKED,

    /** Rede, permissão, ou o que o app não sabe nomear. */
    UNKNOWN,
    ;

    companion object {

        /** A falha do repositório traduzida para a conversa da tela. */
        fun from(failure: LinkRequestFailure): TrainerCodeError = when (failure) {
            LinkRequestFailure.OWN_CODE -> OWN_CODE
            LinkRequestFailure.ALREADY_LINKED -> ALREADY_LINKED
            LinkRequestFailure.UNKNOWN -> UNKNOWN
        }
    }
}

/**
 * A frase que a tela mostra.
 *
 * Mora no arquivo do enum, e não numa lista de mensagens distante: assim um caso novo quebra o
 * `when` na linha de baixo, e não numa tela que ninguém abriu.
 */
@Composable
internal fun TrainerCodeError.message(): String = stringResource(
    when (this) {
        TrainerCodeError.NOT_FOUND -> R.string.student_trainer_error_not_found
        TrainerCodeError.OWN_CODE -> R.string.student_trainer_error_own_code
        TrainerCodeError.ALREADY_LINKED -> R.string.student_trainer_error_already_linked
        TrainerCodeError.UNKNOWN -> R.string.student_trainer_error_unknown
    },
)
