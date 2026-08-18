package com.gabrielfreire.runandlift.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Nome vazio do provedor é **ausência de nome**, e não um nome vazio.
 *
 * Parece detalhe de um caractere e não é: `""` gravado como nome tira do app a única forma de saber
 * que aquela pessoa ainda não escolheu como quer ser chamada. É essa distinção que faz o cadastro
 * escrever o nome do Google só onde não há nenhum — com `""` no lugar de `null`, "já tem nome"
 * passaria a ser verdade para quem não tem, e a tela de conclusão nunca perguntaria.
 */
class ProviderNameTest {

    @Test
    fun `o nome informado passa inteiro`() {
        assertEquals("Ana Souza", ProviderName.of("Ana Souza"))
    }

    @Test
    fun `vazio e so-espacos sao ausencia, que e o que o SDK quis dizer`() {
        // O SDK devolve string vazia, e não nulo, quando o provedor não informou nome.
        assertNull(ProviderName.of(""))
        assertNull(ProviderName.of("   "))
        assertNull(ProviderName.of(null))
    }
}
