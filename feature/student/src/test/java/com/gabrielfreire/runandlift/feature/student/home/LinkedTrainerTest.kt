package com.gabrielfreire.runandlift.feature.student.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * A data do vínculo como ela aparece sob a saudação.
 *
 * O que se confere aqui é o **formato brasileiro**: `DateTimeFormatter` monta a data na ordem que o
 * padrão manda, e um `MM/dd` trocado passa despercebido em toda data até o dia 13 — a metade do mês
 * em que os dois números são plausíveis é exatamente a metade em que ninguém repara no erro.
 *
 * E o que se confere junto é a **ausência**: a data vem de `links/{id}.createdAt`, que chega vazio
 * no documento lido do cache entre a escrita local e a confirmação do servidor. Zero ali tem de
 * virar linha nenhuma, e não 1º de janeiro de 1970 na home de alguém.
 */
class LinkedTrainerTest {

    @Test
    fun `a data do vinculo sai em dia, mes e ano`() {
        val trainer = trainer(LocalDate.of(2026, 3, 9))

        assertEquals("09/03/2026", trainer.sinceLabel)
    }

    @Test
    fun `dia e mes de um digito ganham zero a esquerda`() {
        val trainer = trainer(LocalDate.of(2025, 1, 5))

        assertEquals("05/01/2025", trainer.sinceLabel)
    }

    @Test
    fun `dia depois do dia doze nao vira mes`() {
        val trainer = trainer(LocalDate.of(2025, 2, 28))

        assertEquals("28/02/2025", trainer.sinceLabel)
    }

    @Test
    fun `sem data gravada nao ha linha de -aluno desde-`() {
        assertNull(trainer(since = null).sinceLabel)
    }

    @Test
    fun `carimbo zerado vira ausencia, e nao mil novecentos e setenta`() {
        assertNull(
            "uma data absurda na home é pior do que linha nenhuma",
            LinkedTrainer.dateOf(epochMillis = 0L),
        )
    }

    @Test
    fun `carimbo do servidor vira o dia correspondente`() {
        val date = LinkedTrainer.dateOf(
            epochMillis = LocalDate.of(2026, 3, 9)
                .atTime(12, 0)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
        )

        assertEquals(LocalDate.of(2026, 3, 9), date)
    }

    private fun trainer(since: LocalDate?) = LinkedTrainer(name = "Marcos Vieira", cref = "012345-G/SP", since = since)
}
