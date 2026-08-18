package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * O argumento da lista de cidades, no fluxo de autenticação.
 *
 * O módulo tem o seu, e o do treinador e o do aluno têm os deles: os três grafos não se enxergam, e
 * cada um nomeia o argumento na sua própria constante de rota. Este teste é o que garante que a
 * cópia daqui continua concordando com o padrão de rota daqui.
 */
class UfArgumentTest {

    @Test
    fun `declara um argumento so, com o nome que o padrao da rota usa`() {
        val arguments = ufArgument()

        assertEquals(1, arguments.size)
        assertEquals(AuthRoutes.UF_ARG, arguments.single().name)
    }

    @Test
    fun `a sigla e obrigatoria, ao contrario do perfil`() {
        val argument = ufArgument().single().argument

        // Uma lista de municípios sem estado seriam os 5.571 do país inteiro.
        assertEquals(NavType.StringType, argument.type)
        assertFalse(argument.isNullable)
        assertFalse(argument.isDefaultValuePresent)
    }
}
