package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os dois argumentos de rota do fluxo de autenticação, e a diferença entre eles.
 *
 * São **declarações que precisam concordar com o padrão da rota** e que o compilador não cruza:
 * divergir não quebra a compilação, quebra a navegação — a tela abre sem o dado que motivou passá-lo.
 *
 * A diferença entre os dois é a decisão que estes testes guardam. O perfil é **opcional com padrão
 * nulo**, porque as três telas que o recebem funcionam sem ele: é isso que permite alcançá-las por
 * deep link ou por uma sessão antiga, sem passar pela abertura. A sigla do estado é **obrigatória**,
 * porque uma lista de municípios sem estado seriam os 5.571 do país inteiro — que é o que a tela
 * existe para evitar.
 */
class RoleArgumentTest {

    @Test
    fun `declara um argumento so, com o nome que o padrao da rota usa`() {
        val arguments = roleArgument()

        assertEquals(1, arguments.size)
        assertEquals(AuthRoutes.ROLE_ARG, arguments.single().name)
    }

    @Test
    fun `o perfil e opcional, e sem ele a tela ainda abre`() {
        val argument = roleArgument().single().argument

        assertEquals(NavType.StringType, argument.type)
        assertTrue("obrigatório, ele fecharia o deep link e a sessão antiga", argument.isNullable)
        assertTrue(argument.isDefaultValuePresent)
        assertNull(argument.defaultValue)
    }
}
