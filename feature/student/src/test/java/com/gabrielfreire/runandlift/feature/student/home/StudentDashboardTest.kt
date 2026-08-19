package com.gabrielfreire.runandlift.feature.student.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * As contas do painel do aluno — o que a faixa da semana mostra e o que a frase de apoio diz.
 *
 * A que mais importa é a de [TrainingDayState]: **dia previsto e cumprido vale como cumprido**. É a
 * regra que decide se a semana de quem está em dia aparece com quatro vistos ou com quatro
 * contornos vazios, e um `when` na ordem errada inverte a tela inteira sem quebrar nada.
 */
class StudentDashboardTest {

    @Test
    fun `dia previsto e cumprido conta como cumprido`() {
        val dashboard = dashboard(
            done = setOf(DayOfWeek.MONDAY),
            planned = setOf(DayOfWeek.MONDAY),
        )

        assertEquals(TrainingDayState.DONE, dashboard.stateOn(DayOfWeek.MONDAY))
    }

    @Test
    fun `dia previsto que ainda nao aconteceu fica previsto`() {
        val dashboard = dashboard(done = emptySet(), planned = setOf(DayOfWeek.SATURDAY))

        assertEquals(TrainingDayState.PLANNED, dashboard.stateOn(DayOfWeek.SATURDAY))
    }

    @Test
    fun `dia sem treino previsto e descanso, e nao falta`() {
        val dashboard = dashboard(done = emptySet(), planned = setOf(DayOfWeek.MONDAY))

        assertEquals(TrainingDayState.REST, dashboard.stateOn(DayOfWeek.SUNDAY))
    }

    @Test
    fun `treino registrado fora do previsto ainda conta como feito`() {
        val dashboard = dashboard(done = setOf(DayOfWeek.SUNDAY), planned = setOf(DayOfWeek.MONDAY))

        assertEquals(
            "quem treinou a mais não pode ver o dia em branco",
            TrainingDayState.DONE,
            dashboard.stateOn(DayOfWeek.SUNDAY),
        )
    }

    @Test
    fun `a faixa tem sempre sete dias, comecando na segunda`() {
        val week = StudentDashboard.SAMPLE.week

        assertEquals(7, week.size)
        assertEquals(DayOfWeek.MONDAY, week.first().first)
        assertEquals(DayOfWeek.SUNDAY, week.last().first)
    }

    @Test
    fun `treino a mais nao vira divida negativa`() {
        val dashboard = StudentDashboard.SAMPLE.copy(sessionsDone = 5, sessionsPlanned = 4)

        assertEquals("passar do previsto é boa notícia", 0, dashboard.remainingSessions)
        assertTrue(dashboard.weekComplete)
    }

    @Test
    fun `semana pela metade diz quantos faltam`() {
        val dashboard = StudentDashboard.SAMPLE.copy(sessionsDone = 3, sessionsPlanned = 4)

        assertEquals(1, dashboard.remainingSessions)
        assertFalse(dashboard.weekComplete)
    }

    @Test
    fun `semana sem treino previsto nao se declara completa`() {
        val dashboard = StudentDashboard.SAMPLE.copy(sessionsDone = 0, sessionsPlanned = 0)

        assertFalse(
            "quem ainda não escolheu os dias não fechou semana nenhuma",
            dashboard.weekComplete,
        )
    }

    @Test
    fun `o exemplo da home mostra uma semana com o que ainda falta`() {
        val sample = StudentDashboard.SAMPLE

        assertFalse("uma semana 4 de 4 esconde a única linha que faz alguém sair de casa", sample.weekComplete)
        assertEquals(1, sample.remainingSessions)
        assertTrue(
            "o exemplo precisa dos três estados na faixa para servir de conferência",
            sample.week.map { it.second }.toSet().size == 3,
        )
    }

    private fun dashboard(done: Set<DayOfWeek>, planned: Set<DayOfWeek>) =
        StudentDashboard.SAMPLE.copy(doneDays = done, plannedDays = planned)

    private fun StudentDashboard.stateOn(day: DayOfWeek) = week.first { it.first == day }.second
}
