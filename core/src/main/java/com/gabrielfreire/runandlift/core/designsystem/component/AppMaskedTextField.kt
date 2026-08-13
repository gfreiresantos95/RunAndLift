package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/** Posição da máscara que aceita dígito. */
internal const val MASK_DIGIT = '#'

/** Posição da máscara que aceita letra, sempre gravada em maiúscula. */
internal const val MASK_LETTER = 'A'

/**
 * Campo com máscara — data, telefone, registro profissional, e o que mais vier com formato fixo.
 *
 * O estado é **só o conteúdo**: `"21051990"`, nunca `"21/05/1990"`; `"012345GSP"`, nunca
 * `"012345-G/SP"`. Separador é decoração de apresentação, e guardá-lo no estado obrigaria toda
 * validação e toda gravação a limpá-lo de novo, cada uma do seu jeito.
 *
 * **A máscara é a validação de formato que acontece antes do erro.** O que não couber na posição
 * seguinte não entra: letra onde se espera dígito é descartada na digitação, em vez de virar uma
 * mensagem vermelha depois do envio. Sobra para a validação só o que a máscara não tem como saber —
 * se a data existe, se a sigla do estado é de verdade.
 *
 * Máscara em vez de seletor de calendário para data de nascimento: quem sabe a própria data digita
 * oito dígitos mais rápido do que navega vinte anos de calendário. O teclado numérico já é o alvo
 * grande que o público mais velho precisa (E0-09).
 *
 * @param mask formato com [MASK_DIGIT] no lugar de cada dígito e [MASK_LETTER] no lugar de cada
 *   letra, por exemplo `"##/##/####"` ou `"######-A/AA"`. Qualquer outro caractere é separador,
 *   inserido na exibição e nunca entregue a [onValueChange].
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
    val transformation = remember(mask) { MaskTransformation(mask) }

    AppTextField(
        value = value,
        // Filtrar aqui, e não só no ViewModel: teclado numérico ainda emite vírgula, ponto e sinal
        // em vários aparelhos, e um caractere desses embaralharia o mapeamento de cursor.
        onValueChange = { onValueChange(transformation.sanitize(it)) },
        label = label,
        modifier = modifier,
        errorMessage = errorMessage,
        supportingText = supportingText,
        enabled = enabled,
        // Máscara só de dígitos merece o teclado numérico; máscara com letra não pode tê-lo, ou o
        // campo fica impossível de preencher no aparelho.
        keyboardType = if (transformation.isNumeric) KeyboardType.Number else KeyboardType.Text,
        // O conteúdo vai para maiúscula de qualquer jeito; o teclado combinar com isso evita a
        // sensação de que o aparelho está corrigindo o que se digita.
        capitalization = if (transformation.isNumeric) {
            KeyboardCapitalization.None
        } else {
            KeyboardCapitalization.Characters
        },
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
 * marcadores na máscara — o caractere de índice `k` aparece em `slots[k]`, e o que estiver além do
 * que foi digitado colapsa no fim do texto visível.
 */
@Immutable
internal class MaskTransformation(private val mask: String) : VisualTransformation {

    private val slots: List<Int> = mask.indices.filter { mask[it] == MASK_DIGIT || mask[it] == MASK_LETTER }

    /** Quantos caracteres a máscara comporta. */
    val capacity: Int = slots.size

    /** Máscara sem nenhuma posição de letra — a que pode usar teclado numérico. */
    val isNumeric: Boolean = slots.all { mask[it] == MASK_DIGIT }

    /**
     * Mantém de [input] apenas o que cabe em cada posição, **na ordem**, e descarta o resto.
     *
     * É posicional de propósito: filtrar por "é letra ou dígito" deixaria `AB1234` entrar numa
     * máscara que começa com seis dígitos, e o campo passaria a exibir algo que não é o formato.
     * Descartar na entrada também é o que faz colar `012345-G/SP` funcionar — os separadores do
     * texto colado simplesmente não encontram posição.
     */
    fun sanitize(input: String): String = buildString {
        for (char in input) {
            val slot = slots.getOrNull(length) ?: break

            if (mask[slot] == MASK_DIGIT) {
                if (char.isDigit()) append(char)
            } else {
                if (char.isLetter()) append(char.uppercaseChar())
            }
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val content = sanitize(text.text)
        val masked = buildString {
            var next = 0
            for (symbol in mask) {
                // Sai antes de escrever o separador que ainda não tem caractere depois dele: sem
                // isso, "21" apareceria como "21/" e a barra pareceria algo que o usuário digitou.
                if (next == content.length) break
                if (symbol == MASK_DIGIT || symbol == MASK_LETTER) append(content[next++]) else append(symbol)
            }
        }

        return TransformedText(AnnotatedString(masked), CursorMapping(content.length, masked.length))
    }

    private inner class CursorMapping(private val typed: Int, private val length: Int) : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int = if (offset >= typed) length else slots[offset]

        override fun transformedToOriginal(offset: Int): Int = slots.count { it < offset }.coerceAtMost(typed)
    }
}

/**
 * As três máscaras em uso e o estado que mais engana: a data pela metade, que aparece como `21/05`
 * e **não** como `21/05/` — separador só entra quando há caractere depois dele.
 */
@LightDarkPreviews
@Composable
private fun AppMaskedTextFieldPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                AppMaskedTextField(
                    value = PreviewSamples.Value.BIRTH_DATE_DIGITS,
                    onValueChange = {},
                    label = PreviewSamples.Label.BIRTH_DATE,
                    mask = PreviewSamples.Mask.BIRTH_DATE,
                    supportingText = PreviewSamples.Support.BIRTH_DATE,
                )

                AppMaskedTextField(
                    value = PreviewSamples.Value.BIRTH_DATE_PARTIAL,
                    onValueChange = {},
                    label = PreviewSamples.Label.BIRTH_DATE,
                    mask = PreviewSamples.Mask.BIRTH_DATE,
                    errorMessage = PreviewSamples.Error.BIRTH_DATE_INCOMPLETE,
                )

                AppMaskedTextField(
                    value = PreviewSamples.Value.PHONE_DIGITS,
                    onValueChange = {},
                    label = PreviewSamples.Label.PHONE,
                    mask = PreviewSamples.Mask.PHONE,
                )

                AppMaskedTextField(
                    value = PreviewSamples.Value.CREF_CONTENT,
                    onValueChange = {},
                    label = PreviewSamples.Label.CREF,
                    mask = PreviewSamples.Mask.CREF,
                )
            }
        }
    }
}
