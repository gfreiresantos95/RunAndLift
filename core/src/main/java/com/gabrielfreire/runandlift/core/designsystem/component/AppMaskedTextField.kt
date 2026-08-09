package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo numérico com máscara — data, telefone, e o que mais vier com formato fixo.
 *
 * O estado é **só de dígitos**: `"21051990"`, nunca `"21/05/1990"`. Separador é decoração de
 * apresentação, e guardá-lo no estado obrigaria toda validação e toda gravação a limpá-lo de novo,
 * cada uma do seu jeito.
 *
 * Máscara em vez de seletor de calendário para data de nascimento: quem sabe a própria data digita
 * oito dígitos mais rápido do que navega vinte anos de calendário. O teclado numérico já é o alvo
 * grande que o público mais velho precisa (E0-09).
 *
 * @param mask formato com `#` no lugar de cada dígito, por exemplo `"##/##/####"`. Tudo que não for
 *   `#` é inserido como separador e nunca chega a [onValueChange].
 */
@Composable
fun AppMaskedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    mask: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
) {
    val transformation = remember(mask) { DigitMaskTransformation(mask) }

    AppTextField(
        value = value,
        // Filtrar aqui, e não só no ViewModel: teclado numérico ainda emite vírgula, ponto e sinal
        // em vários aparelhos, e um caractere desses embaralharia o mapeamento de cursor.
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(transformation.digitCount)) },
        label = label,
        modifier = modifier,
        errorMessage = errorMessage,
        supportingText = supportingText,
        enabled = enabled,
        keyboardType = KeyboardType.Number,
        imeAction = imeAction,
        visualTransformation = transformation,
        onImeAction = onImeAction,
    )
}

/**
 * Insere os separadores da máscara na exibição, sem tocar no texto que o campo guarda.
 *
 * A parte que erra com facilidade é o [OffsetMapping]: sem ele o cursor pula para o lugar errado a
 * cada separador atravessado, e selecionar texto quebra. O mapeamento sai direto das posições dos
 * `#` na máscara — o dígito de índice `k` aparece em `slots[k]`, e o que estiver além do que foi
 * digitado colapsa no fim do texto visível.
 */
@Immutable
internal class DigitMaskTransformation(private val mask: String) : VisualTransformation {

    private val slots: List<Int> = mask.indices.filter { mask[it] == '#' }

    /** Quantos dígitos a máscara comporta. */
    val digitCount: Int = slots.size

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit).take(digitCount)
        val masked = buildString {
            var next = 0
            for (symbol in mask) {
                // Sai antes de escrever o separador que ainda não tem dígito depois dele: sem
                // isso, "21" apareceria como "21/" e a barra pareceria algo que o usuário digitou.
                if (next == digits.length) break
                if (symbol == '#') append(digits[next++]) else append(symbol)
            }
        }

        return TransformedText(AnnotatedString(masked), CursorMapping(digits.length, masked.length))
    }

    private inner class CursorMapping(private val digits: Int, private val length: Int) : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int = if (offset >= digits) length else slots[offset]

        override fun transformedToOriginal(offset: Int): Int = slots.count { it < offset }.coerceAtMost(digits)
    }
}
