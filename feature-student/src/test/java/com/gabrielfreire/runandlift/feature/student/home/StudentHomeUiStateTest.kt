package com.gabrielfreire.runandlift.feature.student.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O monograma do card.
 *
 * Regra pequena e cheia de casos de borda: é exatamente o tipo de coisa que um `@Preview` com um
 * nome bonito nunca revela, e que quebra com o primeiro cadastro que tem espaço sobrando no nome.
 */
class StudentHomeUiStateTest {

    @Test
    fun `usa a primeira letra do primeiro nome em maiuscula`() {
        assertEquals("A", StudentHomeUiState(displayName = "Ana Ribeiro").monogram)
    }

    @Test
    fun `nome ja em minuscula vira maiuscula`() {
        assertEquals("J", StudentHomeUiState(displayName = "joão").monogram)
    }

    @Test
    fun `espaco antes do nome nao vira monograma`() {
        assertEquals("A", StudentHomeUiState(displayName = "  Ana").monogram)
    }

    @Test
    fun `sem nome nao ha monograma`() {
        assertNull(StudentHomeUiState(displayName = null).monogram)
    }

    @Test
    fun `nome so de espacos nao ha monograma`() {
        assertNull(StudentHomeUiState(displayName = "   ").monogram)
    }
}
