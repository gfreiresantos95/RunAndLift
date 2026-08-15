package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Retorno tátil de seleção.
 *
 * Existe pelo lugar onde este aplicativo é usado: em pé, na academia, com a mão suada e o olho no
 * espelho ou no relógio da série. Nessa situação o toque não é confirmado pela vista — a pessoa
 * toca, não olha, e segue. Sem vibração ela não tem como saber se o toque pegou, e o resultado é o
 * toque repetido: marca, desmarca, e a resposta acaba errada sem ninguém perceber.
 *
 * **Só em seleção que muda estado**, e nunca em navegação. Vibrar a cada tela aberta transforma o
 * retorno em ruído, e ruído constante é indistinguível de nenhum retorno — a pessoa para de notar.
 *
 * Os dois tipos são distintos de propósito: ligar e desligar dão sensações diferentes, e é isso que
 * permite saber **o que** aconteceu sem olhar, em vez de apenas que algo aconteceu.
 *
 * Respeita a configuração do sistema sozinho — quem desligou a vibração de toque no Android não
 * recebe nada, e não há o que tratar aqui para isso.
 */
@Composable
fun rememberSelectionHaptics(): SelectionHaptics = SelectionHaptics(LocalHapticFeedback.current)

/** Envelope fino em torno do [HapticFeedback], para a tela não escolher o tipo de vibração. */
@JvmInline
value class SelectionHaptics(private val feedback: HapticFeedback) {

    /** Marcou, escolheu, ligou. */
    fun selected() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOn)

    /** Desmarcou, desligou. */
    fun deselected() = feedback.performHapticFeedback(HapticFeedbackType.ToggleOff)

    /** Ligou ou desligou, conforme [selected]. Atalho para quem já tem o estado novo em mãos. */
    fun toggled(selected: Boolean) = if (selected) selected() else deselected()
}
