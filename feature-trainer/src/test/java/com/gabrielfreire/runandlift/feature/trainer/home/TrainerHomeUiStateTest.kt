package com.gabrielfreire.runandlift.feature.trainer.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O monograma do card.
 *
 * Regra pequena e cheia de casos de borda: é exatamente o tipo de coisa que um `@Preview` com um
 * nome bonito nunca revela, e que quebra com o primeiro cadastro que tem espaço sobrando no nome.
 */
class TrainerHomeUiStateTest {

    @Test
    fun `usa a primeira letra do primeiro nome em maiuscula`() {
        assertEquals("C", TrainerHomeUiState(displayName = "Carlos Pereira").monogram)
    }

    @Test
    fun `nome ja em minuscula vira maiuscula`() {
        assertEquals("C", TrainerHomeUiState(displayName = "carlos").monogram)
    }

    @Test
    fun `espaco antes do nome nao vira monograma`() {
        assertEquals("C", TrainerHomeUiState(displayName = "  Carlos").monogram)
    }

    @Test
    fun `sem nome nao ha monograma`() {
        assertNull(TrainerHomeUiState(displayName = null).monogram)
    }

    @Test
    fun `nome so de espacos nao ha monograma`() {
        assertNull(TrainerHomeUiState(displayName = "   ").monogram)
    }
}
