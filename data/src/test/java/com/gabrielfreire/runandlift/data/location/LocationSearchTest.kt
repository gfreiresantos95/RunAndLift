package com.gabrielfreire.runandlift.data.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A régua da busca por nome de localidade.
 *
 * É a regra que decide se alguém encontra a própria cidade, e nenhuma tela a revela: a lista
 * simplesmente aparece vazia quando ela erra. Testá-la aqui é o que impede a busca de exigir um
 * acento que metade das pessoas não digita.
 */
class LocationSearchTest {

    @Test
    fun `acento no nome nao e exigido na busca`() {
        // O teclado com acento custa dois toques por letra, e quem procura "São Paulo" digita
        // "sao paulo". Exigir o acento devolveria lista vazia para uma cidade que existe.
        assertTrue(LocationSearch.matches("São Paulo", "sao paulo"))
        assertTrue(LocationSearch.matches("Jaguarão", "jaguarao"))
        assertTrue(LocationSearch.matches("Ipiranga", "ipiranga"))
    }

    @Test
    fun `acento digitado tambem encontra`() {
        // O caminho inverso: quem digita com acento não pode ser punido por isso.
        assertTrue(LocationSearch.matches("São Paulo", "são"))
        assertTrue(LocationSearch.matches("Santo André", "andré"))
    }

    @Test
    fun `caixa nao conta`() {
        assertTrue(LocationSearch.matches("Belo Horizonte", "BELO"))
        assertTrue(LocationSearch.matches("belo horizonte", "Belo"))
    }

    @Test
    fun `casa no meio do nome, e nao so no comeco`() {
        // "Horizonte" acha "Belo Horizonte": quem lembra da segunda palavra não deveria precisar
        // lembrar da primeira.
        assertTrue(LocationSearch.matches("Belo Horizonte", "horizonte"))
    }

    @Test
    fun `busca vazia casa com tudo`() {
        // É o estado inicial da tela. Uma lista vazia ali diria que não existe estado nenhum.
        assertTrue(LocationSearch.matches("Acre", ""))
    }

    @Test
    fun `o que nao existe nao casa`() {
        assertFalse(LocationSearch.matches("São Paulo", "zzz"))
    }

    @Test
    fun `a sigla tambem encontra, porque ela esta no rotulo`() {
        // O rótulo do estado é "São Paulo - SP", e quem digita "SP" espera achá-lo. Não é uma regra
        // separada: é o que a busca por conteúdo dá de graça.
        assertTrue(LocationSearch.matches("São Paulo - SP", "sp"))
    }
}
