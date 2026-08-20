package com.gabrielfreire.runandlift.feature.student.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * A data do vínculo como ela aparece sob a saudação.
 *
 * O que se confere aqui é o **formato brasileiro**: `DateTimeFormatter` monta a data na ordem que o
 * padrão manda, e um `MM/dd` trocado passa despercebido em toda data até o dia 13 — a metade do mês
 * em que os dois números são plausíveis é exatamente a metade em que ninguém repara no erro.
 */
class LinkedTrainerTest {

    @Test
    fun `a data do vinculo sai em dia, mes e ano`() {
        val trainer = LinkedTrainer.SAMPLE.copy(since = LocalDate.of(2026, 3, 9))

        assertEquals("09/03/2026", trainer.sinceLabel)
    }

    @Test
    fun `dia e mes de um digito ganham zero a esquerda`() {
        val trainer = LinkedTrainer.SAMPLE.copy(since = LocalDate.of(2025, 1, 5))

        assertEquals("05/01/2025", trainer.sinceLabel)
    }

    @Test
    fun `dia depois do dia doze nao vira mes`() {
        val trainer = LinkedTrainer.SAMPLE.copy(since = LocalDate.of(2025, 2, 28))

        assertEquals("28/02/2025", trainer.sinceLabel)
    }

    @Test
    fun `o exemplo da home tem um registro de quem pode prescrever`() {
        assertEquals(
            "categoria G ou P é o que a Lei 9.696 exige de quem prescreve",
            'G',
            LinkedTrainer.SAMPLE.cref[7],
        )
    }
}
