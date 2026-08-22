package com.gabrielfreire.runandlift.feature.student.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Construção das rotas do grafo do aluno.
 *
 * Rota é texto, e texto quebra em silêncio: um padrão registrado que não bate com a rota concreta
 * não falha na compilação — falha na navegação, em produção. É o mesmo teste que o grafo do
 * treinador já tinha, e a razão de existir em dobro é a mesma que faz os dois módulos existirem: eles
 * não se enxergam, e cada um escreve as suas rotas.
 */
class StudentRoutesTest {

    @Test
    fun `o padrao registrado casa com a rota concreta da lista de cidades`() {
        val prefix = StudentRoutes.CITY_PICKER_PATTERN.substringBefore('{')

        assertEquals("${prefix}SP", StudentRoutes.cityPicker("SP"))
        assertEquals("$prefix{${StudentRoutes.UF_ARG}}", StudentRoutes.CITY_PICKER_PATTERN)
    }

    @Test
    fun `a sigla vai no caminho, e nao na consulta`() {
        // Argumento obrigatório é caminho: uma lista de municípios sem estado seriam os 5.571 do
        // país inteiro, que é o que a tela existe para evitar.
        assertTrue(StudentRoutes.cityPicker("MG").endsWith("/MG"))
        assertTrue(StudentRoutes.CITY_PICKER_PATTERN.contains("/{"))
    }

    @Test
    fun `todas as rotas ficam sob o grafo do aluno`() {
        // É o que permite `:app` trocar de papel desempilhando o grafo inteiro por uma rota só — e
        // o que garante que nenhuma tela daqui seja alcançável pela pilha do treinador.
        val routes = listOf(
            StudentRoutes.HOME,
            StudentRoutes.WORKOUTS,
            StudentRoutes.MENU,
            StudentRoutes.ONBOARDING,
            StudentRoutes.PROFILE,
            StudentRoutes.TRAINER,
            StudentRoutes.ACCOUNT,
            StudentRoutes.STATE_PICKER,
            StudentRoutes.CITY_PICKER_PATTERN,
            StudentRoutes.WORKOUT_DAY_PATTERN,
            StudentRoutes.workoutDay(0),
        )

        routes.forEach { assertTrue("$it está fora de ${StudentRoutes.GRAPH}", it.startsWith(StudentRoutes.GRAPH)) }
    }

    @Test
    fun `o dia de treino casa com o padrao registrado`() {
        assertEquals(
            StudentRoutes.WORKOUT_DAY_PATTERN.replace("{${StudentRoutes.DAY_INDEX_ARG}}", "2"),
            StudentRoutes.workoutDay(2),
        )
    }

    @Test
    fun `o dia fica sob a aba de treinos, e nao ao lado dela`() {
        // Não é estética: `sharedStudentWorkoutsViewModel` sobe até a entrada de WORKOUTS para pegar
        // a prescrição já lida. Uma rota irmã empilharia sem manter a aba viva, e cada dia aberto
        // voltaria a custar uma leitura de `assignments`.
        assertTrue(StudentRoutes.workoutDay(0).startsWith("${StudentRoutes.WORKOUTS}/"))
        assertTrue(StudentRoutes.WORKOUT_DAY_PATTERN.startsWith("${StudentRoutes.WORKOUTS}/"))
    }

    @Test
    fun `a posicao vai no caminho, e cada dia tem a sua rota`() {
        assertEquals(3, listOf(0, 1, 2).map { StudentRoutes.workoutDay(it) }.distinct().size)
        assertTrue(StudentRoutes.workoutDay(7).endsWith("/7"))
    }

    @Test
    fun `as tres abas sao rotas irmas, e nenhuma vive dentro de outra`() {
        val tabs = listOf(StudentRoutes.HOME, StudentRoutes.WORKOUTS, StudentRoutes.MENU)

        // Aba não é fluxo: se uma fosse prefixo da outra, a barra inferior estaria escondendo uma
        // pilha aninhada, que é exatamente o que o grafo evita.
        tabs.forEach { tab -> assertTrue(tabs.none { it != tab && it.startsWith("$tab/") }) }
        assertEquals(tabs.size, tabs.distinct().size)
    }
}
