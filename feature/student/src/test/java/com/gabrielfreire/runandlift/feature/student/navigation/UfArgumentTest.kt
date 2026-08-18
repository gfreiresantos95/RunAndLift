package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * O argumento da lista de cidades, no grafo do aluno.
 *
 * São **três declarações que precisam concordar** e que o compilador não cruza: o nome dentro do
 * padrão de rota (`{uf}`), o nome deste argumento, e a chave usada para lê-lo do `Bundle` no grafo.
 * Divergirem não quebra a compilação — quebra a navegação, com a tela abrindo sem saber de qual
 * estado listar municípios.
 */
class UfArgumentTest {

    @Test
    fun `declara um argumento so, com o nome que o padrao da rota usa`() {
        val arguments = ufArgument()

        assertEquals(1, arguments.size)
        assertEquals(StudentRoutes.UF_ARG, arguments.single().name)
    }

    @Test
    fun `o argumento e um texto obrigatorio`() {
        val argument = ufArgument().single().argument

        assertEquals(NavType.StringType, argument.type)
        assertFalse(argument.isNullable)
        assertFalse(argument.isDefaultValuePresent)
    }
}
