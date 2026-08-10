package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Máscara de caracteres e, principalmente, o mapeamento de cursor.
 *
 * O `OffsetMapping` é a parte que o Compose **valida em tempo de execução**: mapeamento que devolve
 * um índice fora do texto derruba o campo com exceção em vez de desalinhar o cursor. Como isso só
 * acontece com o dedo na tela, a garantia tem de vir daqui.
 */
class MaskTransformationTest {

    private val date = MaskTransformation("##/##/####")
    private val phone = MaskTransformation("(##) #####-####")
    private val cref = MaskTransformation("######-A/AA")

    @Test
    fun `insere separador so quando ja existe caractere depois dele`() {
        assertEquals("", date.render(""))
        assertEquals("2", date.render("2"))
        // "21/" faria a barra parecer algo que a pessoa digitou.
        assertEquals("21", date.render("21"))
        assertEquals("21/0", date.render("210"))
        assertEquals("21/05/1990", date.render("21051990"))
    }

    @Test
    fun `descarta o que passa da capacidade da mascara`() {
        assertEquals("21/05/1990", date.render("2105199012"))
        assertEquals(8, date.capacity)
        assertEquals(11, phone.capacity)
        assertEquals(9, cref.capacity)
    }

    @Test
    fun `cada posicao aceita so o que a mascara pede`() {
        // Letra onde se espera dígito não vira erro depois do envio: não chega a entrar.
        assertEquals("", cref.sanitize("abcdef"))
        assertEquals("012345", cref.sanitize("ab012345"))
        assertEquals("012345GSP", cref.sanitize("012345gsp"))
        // A capacidade é por posição, não por tipo: o 7 não ocupa a vaga de letra que sobrou.
        assertEquals("012345G", cref.sanitize("012345G7"))
        assertEquals("21051990", date.sanitize("21/05/1990"))
    }

    @Test
    fun `colar o valor ja formatado funciona, porque separador nao acha posicao`() {
        assertEquals("012345GSP", cref.sanitize("012345-G/SP"))
        assertEquals("012345-G/SP", cref.render("012345-G/SP"))
    }

    @Test
    fun `cursor atravessa o separador sem parar no meio dele`() {
        val mapping = date.filter(AnnotatedString("21051990")).offsetMapping

        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(1, mapping.originalToTransformed(1))
        // Depois do segundo dígito o cursor pula a barra em vez de encostar nela: 2 seria "21|/",
        // e a próxima tecla pareceria ir para antes do separador.
        assertEquals(3, mapping.originalToTransformed(2))
        assertEquals(6, mapping.originalToTransformed(4))
        assertEquals(10, mapping.originalToTransformed(8))
    }

    @Test
    fun `posicao no texto exibido volta para o indice certo do texto guardado`() {
        val mapping = date.filter(AnnotatedString("21051990")).offsetMapping

        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(2, mapping.transformedToOriginal(2))
        assertEquals(2, mapping.transformedToOriginal(3))
        assertEquals(4, mapping.transformedToOriginal(6))
        assertEquals(8, mapping.transformedToOriginal(10))
    }

    @Test
    fun `mapeamento nunca aponta para fora do texto, em nenhum tamanho`() {
        for (mask in listOf(phone, cref)) {
            for (typed in 0..mask.capacity) {
                mask.assertMappingInRange(typed)
            }
        }
    }

    /**
     * Com [typed] caracteres preenchidos, todo offset dos dois lados do mapeamento tem de cair
     * dentro do texto correspondente — é isso que o Compose confere e derruba quando falha.
     */
    private fun MaskTransformation.assertMappingInRange(typed: Int) {
        val content = sanitize("9".repeat(typed) + "Z".repeat(typed))
        val transformed = filter(AnnotatedString(content))
        val visible = transformed.text.length

        for (offset in 0..content.length) {
            val mapped = transformed.offsetMapping.originalToTransformed(offset)
            assertEquals("offset $offset de ${content.length} guardados", true, mapped in 0..visible)
        }

        for (offset in 0..visible) {
            val mapped = transformed.offsetMapping.transformedToOriginal(offset)
            assertEquals("offset $offset de $visible visíveis", true, mapped in 0..content.length)
        }
    }

    private fun MaskTransformation.render(input: String): String = filter(AnnotatedString(input)).text.text
}
