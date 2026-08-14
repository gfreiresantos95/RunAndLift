package com.gabrielfreire.runandlift.feature.auth.validation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * O que pode faltar no e-mail antes de valer a pena perguntar ao servidor.
 *
 * O enum e a frase que ele vira moram no **mesmo arquivo** de propósito. Acrescentar um caso aqui
 * quebra o `when` de [message] na linha de baixo, e não num arquivo de mensagens que se descobre
 * depois — a exaustividade do compilador só é uma rede de segurança se ela cair perto de quem mexeu.
 *
 * A divisão de responsabilidade continua a mesma de antes: o **ViewModel expõe o motivo**, e nunca
 * o texto, o que é o que permite testá-lo sem Android. Quem traduz motivo em frase é a UI — e é o
 * que [message] é, uma função `@Composable` que só a tela chama.
 */
internal enum class EmailError { REQUIRED, INVALID }

@Composable
internal fun EmailError.message(): String = stringResource(
    when (this) {
        EmailError.REQUIRED -> R.string.auth_error_email_required
        EmailError.INVALID -> R.string.auth_error_email_invalid
    },
)
