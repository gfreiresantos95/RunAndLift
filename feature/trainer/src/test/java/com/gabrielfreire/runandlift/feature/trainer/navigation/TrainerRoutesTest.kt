package com.gabrielfreire.runandlift.feature.trainer.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Construção das rotas do grafo do treinador.
 *
 * Rota é texto, e texto quebra em silêncio: um padrão registrado que não bate com a rota concreta
 * não falha na compilação — falha na navegação, em produção. É a mesma razão do `AuthRoutesTest`,
 * aplicada ao único par padrão/rota deste grafo: a lista de cidades.
 */
class TrainerRoutesTest {

    @Test
    fun `o padrao registrado casa com a rota concreta da lista de cidades`() {
        val prefix = TrainerRoutes.CITY_PICKER_PATTERN.substringBefore('{')

        assertTrue(TrainerRoutes.cityPicker("SP").startsWith(prefix))
        assertEquals("${prefix}SP", TrainerRoutes.cityPicker("SP"))
        assertEquals("$prefix{${TrainerRoutes.UF_ARG}}", TrainerRoutes.CITY_PICKER_PATTERN)
    }

    @Test
    fun `a sigla vai no caminho, e nao na consulta`() {
        // Uma lista de municípios sem estado seriam os 5.571 do país inteiro, que é o que a tela
        // existe para evitar — por isso o argumento é obrigatório, e argumento obrigatório é caminho.
        assertTrue(TrainerRoutes.cityPicker("MG").endsWith("/MG"))
        assertTrue(TrainerRoutes.CITY_PICKER_PATTERN.contains("/{"))
    }

    @Test
    fun `todas as rotas ficam sob o grafo do treinador`() {
        // É o que permite `:app` trocar de papel desempilhando o grafo inteiro por uma rota só — e
        // o que garante que nenhuma tela daqui seja alcançável pela pilha do aluno.
        val routes = listOf(
            TrainerRoutes.HOME,
            TrainerRoutes.WORKOUTS,
            TrainerRoutes.MENU,
            TrainerRoutes.ONBOARDING,
            TrainerRoutes.PROFILE,
            TrainerRoutes.ACCOUNT,
            TrainerRoutes.STATE_PICKER,
            TrainerRoutes.CITY_PICKER_PATTERN,
            TrainerRoutes.cityPicker("RJ"),
        )

        routes.forEach {
            assertTrue("$it deveria começar com ${TrainerRoutes.GRAPH}/", it.startsWith("${TrainerRoutes.GRAPH}/"))
        }
    }
}
