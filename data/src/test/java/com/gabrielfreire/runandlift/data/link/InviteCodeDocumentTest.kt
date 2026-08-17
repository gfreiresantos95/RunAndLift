package com.gabrielfreire.runandlift.data.link

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * O alfabeto do código de convite e a limpeza do que foi digitado.
 *
 * São as duas regras deste fluxo que só se conferem tentando: o alfabeto decide se um código ditado
 * por telefone chega inteiro do outro lado, e a normalização decide se quem digitou com espaço, em
 * minúsculo ou com hífen encontra o treinador — ou vê "código não encontrado" tendo acertado.
 */
class InviteCodeDocumentTest {

    @Test
    fun `o codigo tem seis caracteres`() {
        assertEquals(6, InviteCodeDocument.newCode(Random(seed = 1)).length)
    }

    @Test
    fun `o codigo nunca traz os quatro caracteres que ninguem distingue`() {
        val sorteados = (1..200).joinToString(separator = "") { InviteCodeDocument.newCode(Random(seed = it)) }

        // O, 0, I e 1: o que se perde de combinações é irrelevante perto de alguém digitar
        // corretamente aquilo que enxergou errado.
        assertFalse(sorteados.any { it in "O0I1" })
    }

    @Test
    fun `o codigo so usa maiuscula e digito`() {
        val codigo = InviteCodeDocument.newCode(Random(seed = 7))

        assertTrue(codigo.all { it.isUpperCase() || it.isDigit() })
    }

    @Test
    fun `dois sorteios seguidos nao sao o mesmo codigo`() {
        val random = Random(seed = 42)

        assertFalse(InviteCodeDocument.newCode(random) == InviteCodeDocument.newCode(random))
    }

    @Test
    fun `minuscula digitada vira o codigo certo`() {
        assertEquals("ABC234", InviteCodeDocument.normalize("abc234"))
    }

    @Test
    fun `espaco e hifen sao descartados, porque quem os digitou acertou o codigo`() {
        assertEquals("ABC234", InviteCodeDocument.normalize(" abc-234 "))
    }

    @Test
    fun `caractere fora do alfabeto some, e o que sobra e que decide`() {
        // O `0` está fora do alfabeto de propósito: adivinhar que a pessoa quis dizer `O` erraria
        // em silêncio, e o que ela recebe é a resposta honesta de que aquele código não existe.
        assertEquals("ABC23", InviteCodeDocument.normalize("ABC0-23"))
    }

    @Test
    fun `o convite guarda o dono e o nome com que ele se apresenta`() {
        val fields = InviteCodeDocument.fields(trainerId = "treinador-1", trainerName = "Carlos Pereira")

        assertEquals("treinador-1", fields[InviteCodeDocument.FIELD_TRAINER_ID])
        assertEquals("Carlos Pereira", fields[InviteCodeDocument.FIELD_TRAINER_NAME])
    }
}
