package com.gabrielfreire.runandlift.feature.trainer.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os cortes do semáforo de aderência, e a coerência do exemplo que a home mostra hoje.
 *
 * Os cortes têm teste porque são a única regra dentro do painel do treinador: 80% e 50% decidem a
 * cor de um número que o produto inteiro usa para dizer quem está treinando e quem parou, e são
 * constantes que ninguém pensaria duas vezes antes de "ajustar".
 *
 * O exemplo tem teste por outra razão: ele é desenhado na tela de gente de verdade, e um exemplo
 * incoerente — mais check-ins respondidos do que enviados — é um bug que só apareceria numa captura
 * de tela mandada por alguém.
 */
class TrainerDashboardTest {

    @Test
    fun `oitenta por cento ja e em dia`() {
        assertEquals(AttentionLevel.OK, dashboard(adherence = 80).level)
    }

    @Test
    fun `abaixo de oitenta escorrega`() {
        assertEquals(AttentionLevel.SLIPPING, dashboard(adherence = 79).level)
    }

    @Test
    fun `metade ainda escorrega, e nao parou`() {
        assertEquals(AttentionLevel.SLIPPING, dashboard(adherence = 50).level)
    }

    @Test
    fun `abaixo da metade nao e um programa em andamento`() {
        assertEquals(AttentionLevel.STOPPED, dashboard(adherence = 49).level)
    }

    @Test
    fun `carteira sem aderencia nenhuma cai no pior nivel`() {
        assertEquals(AttentionLevel.STOPPED, dashboard(adherence = 0).level)
    }

    @Test
    fun `o exemplo da home mostra uma semana boa com um problema`() {
        val sample = TrainerDashboard.SAMPLE

        assertEquals(
            "uma semana redonda deixaria vazio o único bloco que muda o que o treinador faz",
            AttentionLevel.OK,
            sample.level,
        )
        assertTrue("sem ninguém na lista, o bloco de atenção não prova nada", sample.attentionCount > 0)
    }

    @Test
    fun `o exemplo nao responde mais check-ins do que enviou`() {
        val sample = TrainerDashboard.SAMPLE

        assertTrue(sample.checkInsAnswered <= sample.checkInsSent)
    }

    @Test
    fun `so quem esta em dia leva o visto`() {
        assertNotEquals(AttentionLevel.OK.icon(), AttentionLevel.STOPPED.icon())
        assertEquals(
            "escorregar e parar são graus da mesma coisa, e o que os separa é a cor e a palavra",
            AttentionLevel.SLIPPING.icon(),
            AttentionLevel.STOPPED.icon(),
        )
    }

    private fun dashboard(adherence: Int) = TrainerDashboard.SAMPLE.copy(adherence = adherence)
}
