package com.gabrielfreire.runandlift.feature.auth.navigation

import com.gabrielfreire.runandlift.data.model.ActiveRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Construção das rotas do fluxo de entrada.
 *
 * Rota é texto, e texto quebra em silêncio: um padrão registrado que não bate com a rota concreta
 * não falha na compilação — falha na navegação, em produção. O que se afirma aqui é que as duas
 * formas continuam combinando.
 */
class AuthRoutesTest {

    @Test
    fun `rota sem papel nao leva argumento`() {
        // As três telas são alcançáveis sem perfil conhecido — por sessão antiga ou por deep link.
        assertEquals("auth/sign-in", AuthRoutes.signIn(null))
        assertEquals("auth/sign-up", AuthRoutes.signUp(null))
        assertEquals("auth/complete-profile", AuthRoutes.completeProfile(null))
    }

    @Test
    fun `rota com papel usa o valor gravado no Firestore`() {
        // E não o `name` do enum: o valor de armazenamento é fixo, e é ele que volta pela rota.
        assertEquals("auth/sign-in?role=student", AuthRoutes.signIn(ActiveRole.STUDENT))
        assertEquals("auth/sign-up?role=trainer", AuthRoutes.signUp(ActiveRole.TRAINER))
        assertEquals("auth/complete-profile?role=trainer", AuthRoutes.completeProfile(ActiveRole.TRAINER))
    }

    @Test
    fun `o padrao registrado casa com a rota concreta`() {
        // O padrão declara `?role={role}` e a rota concreta preenche o valor. Se um dos dois mudar
        // de nome de argumento, o destino deixa de ser encontrado — sem erro de compilação.
        val prefix = AuthRoutes.SIGN_IN_PATTERN.substringBefore('?')

        assertTrue(AuthRoutes.signIn(ActiveRole.STUDENT).startsWith(prefix))
        assertEquals("$prefix?${AuthRoutes.ROLE_ARG}={${AuthRoutes.ROLE_ARG}}", AuthRoutes.SIGN_IN_PATTERN)
    }

    @Test
    fun `todas as rotas do fluxo ficam sob o grafo de entrada`() {
        // É o que permite `:app` desempilhar o fluxo inteiro por uma rota só depois de autenticar.
        val routes = listOf(
            AuthRoutes.WELCOME,
            AuthRoutes.RECOVERY,
            AuthRoutes.ROLE_SELECTION,
            AuthRoutes.signIn(),
            AuthRoutes.signUp(),
            AuthRoutes.completeProfile(),
        )

        routes.forEach {
            assertTrue("$it deveria começar com ${AuthRoutes.GRAPH}/", it.startsWith("${AuthRoutes.GRAPH}/"))
        }
    }
}
