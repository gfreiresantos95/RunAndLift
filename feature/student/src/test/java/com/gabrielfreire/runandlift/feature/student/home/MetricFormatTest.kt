package com.gabrielfreire.runandlift.feature.student.home

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Os dois formatos do painel: separador de milhar e tempo de treino.
 *
 * São regras escritas à mão em vez de delegadas ao `NumberFormat` do sistema — a razão está no
 * arquivo —, e regra escrita à mão sem teste é regra que erra na fronteira. As fronteiras aqui são
 * três: exatamente mil, exatamente uma hora, e o zero à esquerda dos minutos.
 */
class MetricFormatTest {

    @Test
    fun `abaixo de mil nada muda`() {
        assertEquals("999", 999.asGroupedNumber())
    }

    @Test
    fun `mil ja ganha separador`() {
        assertEquals("1.000", 1_000.asGroupedNumber())
    }

    @Test
    fun `volume de uma semana de verdade fica legivel`() {
        assertEquals("12.480", 12_480.asGroupedNumber())
    }

    @Test
    fun `numero de sete digitos ganha os dois separadores`() {
        assertEquals("1.234.567", 1_234_567.asGroupedNumber())
    }

    @Test
    fun `zero e zero`() {
        assertEquals("0", 0.asGroupedNumber())
    }

    @Test
    fun `o sinal fica fora do agrupamento`() {
        assertEquals("-1.500", (-1_500).asGroupedNumber())
    }

    @Test
    fun `abaixo de uma hora nao inventa hora zero`() {
        assertEquals("45min", 45.asDuration())
    }

    @Test
    fun `uma hora redonda mostra os minutos zerados`() {
        assertEquals("1h00", 60.asDuration())
    }

    @Test
    fun `minuto de um digito ganha zero a esquerda`() {
        assertEquals("3h07", 187.asDuration())
    }

    @Test
    fun `o tempo de treino do exemplo`() {
        assertEquals("3h17", 197.asDuration())
    }

    @Test
    fun `semana sem treino nenhum e zero minuto`() {
        assertEquals("0min", 0.asDuration())
    }
}
