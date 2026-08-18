package com.gabrielfreire.runandlift.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

/**
 * Qual exceção do Firebase vira qual motivo.
 *
 * O teste que sustenta os outros é o da **ordem**: as classes de exceção do SDK herdam umas das
 * outras, e `FirebaseAuthWeakPasswordException` **é** uma `FirebaseAuthInvalidCredentialsException`.
 * Trocada a ordem dos ramos, quem escolheu uma senha de seis caracteres é mandado conferir o e-mail
 * e a senha que digitou certos — e vai conferir, e vai continuar sem conseguir criar a conta.
 *
 * O segundo em importância é o par "conta não existe" / "senha errada" caindo na **mesma** resposta.
 * Não é imprecisão: separá-las transformaria a tela de entrada num verificador de quem tem cadastro
 * neste app, que é exatamente o que um formulário de login não pode ser.
 */
class AuthFailureMappingTest {

    @Test
    fun `senha fraca ganha a propria resposta, apesar de ser credencial invalida para o SDK`() {
        val failure = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "curta demais", "PASSWORD_TOO_SHORT")

        assertEquals(AuthFailure.WEAK_PASSWORD, AuthFailureMapping.reasonFor(failure))
    }

    @Test
    fun `conta que nao existe e senha errada dao a mesma resposta`() {
        val inexistente = FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "sem conta")
        val errada = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "senha errada")

        // Distingui-las diria a um estranho quem tem cadastro aqui.
        assertEquals(AuthFailure.INVALID_CREDENTIALS, AuthFailureMapping.reasonFor(inexistente))
        assertEquals(AuthFailure.INVALID_CREDENTIALS, AuthFailureMapping.reasonFor(errada))
    }

    @Test
    fun `tentativa demais tem resposta propria, porque tentar de novo agora nao adianta`() {
        val failure = FirebaseTooManyRequestsException("bloqueado por excesso de tentativas")

        assertEquals(AuthFailure.TOO_MANY_ATTEMPTS, AuthFailureMapping.reasonFor(failure))
    }

    @Test
    fun `falta de rede e falta de rede, venha do Firebase ou do socket`() {
        assertEquals(AuthFailure.NO_NETWORK, AuthFailureMapping.reasonFor(FirebaseNetworkException("sem rede")))
        assertEquals(AuthFailure.NO_NETWORK, AuthFailureMapping.reasonFor(UnknownHostException("dns")))
        assertEquals(AuthFailure.NO_NETWORK, AuthFailureMapping.reasonFor(IOException("conexão caiu")))
    }

    @Test
    fun `o que nao se sabe nomear ainda vira uma frase`() {
        // Sem este ramo, uma exceção nova do SDK subiria e fecharia o app. "Não foi possível
        // entrar" é sempre melhor do que a tela sumir.
        assertEquals(AuthFailure.UNKNOWN, AuthFailureMapping.reasonFor(IllegalStateException("algo novo")))
    }
}
