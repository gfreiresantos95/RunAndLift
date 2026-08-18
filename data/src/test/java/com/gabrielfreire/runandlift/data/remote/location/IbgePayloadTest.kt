package com.gabrielfreire.runandlift.data.remote.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O que a resposta do IBGE vira, e principalmente **em que ordem**.
 *
 * A ordenação é a regra que ninguém confere lendo o código e que qualquer um nota usando: a API
 * ordena por código de caractere, régua na qual "Águas Claras" cai **depois** de "Zabelê". Numa
 * lista de 853 nomes, um punhado de acentuadas exiladas no fim não parece decisão de ordenação —
 * parece que o app perdeu as cidades e as devolveu no lugar errado.
 *
 * O segundo grupo guarda a conferência da sigla, que existe porque ela é concatenada dentro de uma
 * URL. Sem ela, valor vindo de fora entra no caminho da requisição.
 */
class IbgePayloadTest {

    @Test
    fun `os estados saem com sigla e nome`() {
        val states = IbgePayload.states(
            """[{"id":35,"sigla":"SP","nome":"São Paulo"},{"id":31,"sigla":"MG","nome":"Minas Gerais"}]""",
        )

        assertEquals(listOf("MG", "SP"), states.map { it.uf })
        assertEquals("Minas Gerais", states.first().name)
    }

    @Test
    fun `acentuada nao vai para o fim da lista de estados`() {
        val states = IbgePayload.states(
            """[{"sigla":"SP","nome":"São Paulo"},{"sigla":"ES","nome":"Espírito Santo"},
               |{"sigla":"AM","nome":"Amazonas"}]
            """.trimMargin(),
        )

        // Por código de caractere, "Espírito Santo" cairia depois de "São Paulo".
        assertEquals(listOf("Amazonas", "Espírito Santo", "São Paulo"), states.map { it.name })
    }

    @Test
    fun `municipio acentuado fica onde a pessoa vai procura-lo`() {
        val cities = IbgePayload.cities("""[{"nome":"Zabelê"},{"nome":"Águas Claras"},{"nome":"Belém"}]""")

        // O caso exato que a régua do IBGE erra: `Á` tem código maior que `Z`.
        assertEquals(listOf("Águas Claras", "Belém", "Zabelê"), cities)
    }

    @Test
    fun `a hierarquia territorial aninhada e ignorada, e so o nome sai`() {
        // É o formato real: cada município traz microrregião, mesorregião, UF e região dentro.
        val cities = IbgePayload.cities(
            """[{"id":3106200,"nome":"Belo Horizonte","microrregiao":{"id":31030,"nome":"Belo Horizonte",
               |"mesorregiao":{"id":3107,"nome":"Metropolitana","UF":{"id":31,"sigla":"MG","nome":"Minas Gerais"}}}}]
            """.trimMargin(),
        )

        assertEquals(listOf("Belo Horizonte"), cities)
    }

    @Test
    fun `resposta vazia e uma lista vazia, e nao um erro`() {
        assertEquals(emptyList<String>(), IbgePayload.cities("[]"))
    }

    @Test
    fun `campo obrigatorio ausente e erro, porque a resposta nao e a que esperavamos`() {
        val failure = assertThrows(IllegalArgumentException::class.java) {
            IbgePayload.states("""[{"id":35,"nome":"São Paulo"}]""")
        }

        assertTrue(failure.message.orEmpty().contains(IbgePayload.FIELD_UF))
    }

    // -- A sigla que entra na URL ---------------------------------------------------------------

    @Test
    fun `a sigla sobe para maiuscula`() {
        assertEquals("MG", IbgePayload.requireUf("mg"))
    }

    @Test
    fun `o que nao e duas letras nao entra no caminho da requisicao`() {
        listOf("", "S", "SPX", "S1", "../estados", "SP MG").forEach {
            assertThrows("aceitar `$it` poria valor de fora dentro da URL", IllegalArgumentException::class.java) {
                IbgePayload.requireUf(it)
            }
        }
    }
}
