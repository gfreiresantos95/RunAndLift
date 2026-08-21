package com.gabrielfreire.runandlift.feature.trainer.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Construção das rotas do grafo do treinador.
 *
 * Rota é texto, e texto quebra em silêncio: um padrão registrado que não bate com a rota concreta
 * não falha na compilação — falha na navegação, em produção. É a mesma razão do `AuthRoutesTest`,
 * aplicada agora a seis pares padrão/rota: a lista de cidades e as cinco telas da montagem de
 * treino, que são as que empilham argumento sobre argumento.
 *
 * A montagem é o caso extremo desse risco. A prescrição carrega **três** argumentos no caminho — o
 * programa, o dia e o exercício —, cada um posto por uma tela diferente, e o padrão que os registra
 * é montado por concatenação a partir do padrão do dia, que por sua vez vem do padrão do editor. Uma
 * barra a mais em qualquer degrau só aparece quando alguém toca no exercício.
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
    fun `o editor de programa casa com o padrao registrado`() {
        assertEquals(
            TrainerRoutes.PROGRAM_EDITOR_PATTERN.replace("{${TrainerRoutes.PROGRAM_ID_ARG}}", "p1"),
            TrainerRoutes.programEditor("p1"),
        )
    }

    @Test
    fun `criar um programa e o mesmo destino, com uma palavra reservada no lugar do id`() {
        // Uma rota separada para criar seria a mesma tela, com o mesmo formulário e a mesma
        // gravação — a única diferença, ler antes de mostrar, cabe num `if`.
        assertEquals(TrainerRoutes.programEditor(), TrainerRoutes.programEditor(TrainerRoutes.NEW_PROGRAM))
        assertTrue(TrainerRoutes.programEditor().endsWith("/${TrainerRoutes.NEW_PROGRAM}"))
    }

    @Test
    fun `o dia e a prescricao empilham sobre o editor sem perder argumento`() {
        val dia = TrainerRoutes.dayEditor(programId = "p1", dayIndex = 2)
        val prescricao = TrainerRoutes.prescription(programId = "p1", dayIndex = 2, exerciseIndex = 4)

        assertTrue("o dia nasce do editor", dia.startsWith(TrainerRoutes.programEditor("p1")))
        assertTrue("a prescrição nasce do dia", prescricao.startsWith(dia))
        assertEquals(
            TrainerRoutes.DAY_EDITOR_PATTERN
                .replace("{${TrainerRoutes.PROGRAM_ID_ARG}}", "p1")
                .replace("{${TrainerRoutes.DAY_INDEX_ARG}}", "2"),
            dia,
        )
        assertEquals(
            TrainerRoutes.PRESCRIPTION_PATTERN
                .replace("{${TrainerRoutes.PROGRAM_ID_ARG}}", "p1")
                .replace("{${TrainerRoutes.DAY_INDEX_ARG}}", "2")
                .replace("{${TrainerRoutes.EXERCISE_INDEX_ARG}}", "4"),
            prescricao,
        )
    }

    @Test
    fun `atribuir empilha sobre o editor e casa com o padrao`() {
        assertEquals(
            TrainerRoutes.ASSIGN_PATTERN.replace("{${TrainerRoutes.PROGRAM_ID_ARG}}", "p1"),
            TrainerRoutes.assign("p1"),
        )
    }

    @Test
    fun `a ficha do exercicio casa com o padrao registrado`() {
        assertEquals(
            TrainerRoutes.EXERCISE_DETAIL_PATTERN.replace("{${TrainerRoutes.EXERCISE_ID_ARG}}", "supino"),
            TrainerRoutes.exerciseDetail("supino"),
        )
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
            TrainerRoutes.CATALOG,
            TrainerRoutes.EXERCISE_DETAIL_PATTERN,
            TrainerRoutes.exerciseDetail("supino"),
            TrainerRoutes.PROGRAM_EDITOR_PATTERN,
            TrainerRoutes.programEditor("p1"),
            TrainerRoutes.dayEditor("p1", 0),
            TrainerRoutes.prescription("p1", 0, 0),
            TrainerRoutes.assign("p1"),
        )

        routes.forEach {
            assertTrue("$it deveria começar com ${TrainerRoutes.GRAPH}/", it.startsWith("${TrainerRoutes.GRAPH}/"))
        }
    }
}
