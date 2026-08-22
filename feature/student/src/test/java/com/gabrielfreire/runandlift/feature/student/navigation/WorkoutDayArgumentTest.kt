package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O argumento do dia de treino.
 *
 * Mesma armadilha do [ufArgument]: são **três declarações que precisam concordar** e que o
 * compilador não cruza — o nome dentro do padrão de rota (`{dayIndex}`), o nome deste argumento e a
 * chave que o destino usa para lê-lo do `Bundle`. Divergirem não quebra a compilação; quebra a tela,
 * que abre sem saber qual dia mostrar.
 *
 * O tipo tem peso próprio aqui. `IntType` é o que faz uma rota com letra no lugar do número
 * simplesmente **não casar** com o padrão, em vez de casar e abrir a tela com um dia nulo — e é o
 * que dispensa um `toIntOrNull()` no destino.
 */
class WorkoutDayArgumentTest {

    @Test
    fun `declara um argumento so, com o nome que o padrao da rota usa`() {
        val arguments = workoutDayArgument()

        assertEquals(1, arguments.size)
        assertEquals(StudentRoutes.DAY_INDEX_ARG, arguments.single().name)
        assertTrue(StudentRoutes.WORKOUT_DAY_PATTERN.contains("{${arguments.single().name}}"))
    }

    @Test
    fun `o argumento e um inteiro obrigatorio`() {
        val argument = workoutDayArgument().single().argument

        assertEquals(NavType.IntType, argument.type)
        assertFalse(argument.isNullable)
        // Sem valor padrão de propósito: um dia que abre no índice zero por omissão mostraria o
        // treino A para quem tocou no C.
        assertFalse(argument.isDefaultValuePresent)
    }
}
