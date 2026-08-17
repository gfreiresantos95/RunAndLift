package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * O argumento da lista de cidades.
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
        assertEquals(TrainerRoutes.UF_ARG, arguments.single().name)
    }

    @Test
    fun `o argumento e um texto obrigatorio`() {
        val argument = ufArgument().single().argument

        // Texto porque a sigla é texto, e obrigatório porque uma lista de municípios sem estado
        // seriam os 5.571 do país inteiro — que é o que a tela existe para evitar.
        assertEquals(NavType.StringType, argument.type)
        assertFalse(argument.isNullable)
        assertFalse(argument.isDefaultValuePresent)
    }
}
