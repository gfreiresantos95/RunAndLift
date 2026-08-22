package com.gabrielfreire.runandlift.feature.student.workouts

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A carga e a linha de números como o aluno as lê na academia.
 *
 * A carga é o caso que parece bobo e não é: `Double` existe porque 62,5 kg é a soma de duas anilhas
 * de 1,25, mas a maioria das cargas é inteira — e `toString()` de um `Double` escreve "60.0", com
 * ponto e com uma casa que sugere uma precisão que a anilha não tem.
 */
class PrescriptionFormatTest {

    @Test
    fun `carga inteira perde a casa decimal`() {
        assertEquals("60", PrescriptionFormat.load(60.0))
        assertEquals("0", PrescriptionFormat.load(0.0))
    }

    @Test
    fun `a meia casa sobrevive, com virgula`() {
        // 62,5 é a soma de duas anilhas de 1,25 — existe de verdade, e em português se escreve com
        // vírgula.
        assertEquals("62,5", PrescriptionFormat.load(62.5))
        assertEquals("7,5", PrescriptionFormat.load(7.5))
    }

    @Test
    fun `a linha junta os pedacos com o separador da planilha`() {
        assertEquals("4 × 8-12 · 60 kg · 90 s", PrescriptionFormat.summary(listOf("4 × 8-12", "60 kg", "90 s")))
    }

    @Test
    fun `o que nao foi prescrito some, em vez de virar sem carga`() {
        // Ocupar espaço para dizer que não há o que dizer é o que transforma uma linha legível de
        // relance numa frase.
        assertEquals("3 × 30", PrescriptionFormat.summary(listOf("3 × 30", null, null)))
        assertEquals("3 × 30 · 45 s", PrescriptionFormat.summary(listOf("3 × 30", null, "45 s")))
    }
}
