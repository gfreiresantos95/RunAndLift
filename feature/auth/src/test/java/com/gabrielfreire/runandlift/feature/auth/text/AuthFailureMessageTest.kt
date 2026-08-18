package com.gabrielfreire.runandlift.feature.auth.text

import com.gabrielfreire.runandlift.data.auth.AuthFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Cada falha tem a sua frase — e as que **compartilham** frase, compartilham de propósito.
 *
 * Não se afirma aqui qual é o texto: isso é `strings.xml`, e repeti-lo num teste seria manter a
 * mesma frase em dois lugares para descobrir, no dia da mudança, que só um deles mudou. O que se
 * afirma é o que a tradução decide: quantas conversas diferentes existem.
 *
 * Duas decisões moram nesse número. `NOT_SIGNED_IN` e `UNKNOWN` caem juntas porque nenhuma das duas
 * tem o que dizer a quem está olhando — "sessão perdida" é vocabulário de quem escreveu o app, não
 * de quem o usa. E `NO_GOOGLE_ACCOUNT` **não** cai ali junto, apesar de ser rara: ela tem solução
 * própria, e mandar adicionar uma conta no aparelho é conselho que resolve.
 */
class AuthFailureMessageTest {

    @Test
    fun `todo motivo tem frase, e nenhum cai no vazio`() {
        AuthFailure.entries.forEach {
            assertNotEquals("$it ficaria sem mensagem na tela", 0, it.messageRes())
        }
    }

    @Test
    fun `sessao perdida e falha sem nome dizem a mesma coisa`() {
        // Nenhuma das duas tem o que dizer a quem está olhando; a diferença entre elas é da
        // telemetria, e a telemetria lê `cause`, não a tela.
        assertEquals(AuthFailure.UNKNOWN.messageRes(), AuthFailure.NOT_SIGNED_IN.messageRes())
    }

    @Test
    fun `falta de conta Google tem frase propria, porque tem solucao propria`() {
        assertNotEquals(AuthFailure.UNKNOWN.messageRes(), AuthFailure.NO_GOOGLE_ACCOUNT.messageRes())
    }

    @Test
    fun `os erros que quem digita resolve nao se confundem entre si`() {
        val distinct = listOf(
            AuthFailure.INVALID_CREDENTIALS,
            AuthFailure.EMAIL_ALREADY_IN_USE,
            AuthFailure.WEAK_PASSWORD,
            AuthFailure.INVALID_EMAIL,
            AuthFailure.NO_NETWORK,
            AuthFailure.TOO_MANY_ATTEMPTS,
        ).map { it.messageRes() }

        // Cada um pede uma ação diferente de quem está na tela — juntá-los faria a pessoa tentar
        // de novo exatamente nos casos em que tentar de novo não muda nada.
        assertEquals(distinct.size, distinct.distinct().size)
    }
}
