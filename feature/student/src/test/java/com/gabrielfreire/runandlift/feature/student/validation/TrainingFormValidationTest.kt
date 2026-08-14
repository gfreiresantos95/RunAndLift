package com.gabrielfreire.runandlift.feature.student.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A régua de peso e altura.
 *
 * O que ela existe para pegar é **erro de digitação**, não corpo fora de um padrão: 7 no lugar de
 * 70, e 1,75 no lugar de 175. Por isso as faixas são largas, e por isso campo vazio passa — os dois
 * são opcionais, e o onboarding deixa pular.
 */
class TrainingFormValidationTest {

    @Test
    fun `campo vazio nao e erro`() {
        assertNull(TrainingFormValidation.validateWeight(""))
        assertNull(TrainingFormValidation.validateWeight("   "))
        assertNull(TrainingFormValidation.validateHeight(""))
    }

    @Test
    fun `peso aceita virgula, que e o separador do portugues`() {
        assertNull(TrainingFormValidation.validateWeight("72,5"))
        assertEquals(72.5, TrainingFormValidation.parseWeight("72,5"))
    }

    @Test
    fun `peso fora da faixa e erro de digitacao`() {
        // 7 em vez de 70 — o caso que motiva a régua existir.
        assertEquals(WeightError.INVALID, TrainingFormValidation.validateWeight("7"))
        assertEquals(WeightError.INVALID, TrainingFormValidation.validateWeight("500"))
        assertEquals(WeightError.INVALID, TrainingFormValidation.validateWeight("setenta"))
    }

    @Test
    fun `peso alto passa, porque a regua nao opina sobre corpo`() {
        assertNull(TrainingFormValidation.validateWeight("140"))
        assertNull(TrainingFormValidation.validateWeight("180"))
    }

    @Test
    fun `altura em metros e recusada, que e o erro de unidade mais comum`() {
        assertEquals(HeightError.INVALID, TrainingFormValidation.validateHeight("1,75"))
        assertEquals(HeightError.INVALID, TrainingFormValidation.validateHeight("2"))
        assertNull(TrainingFormValidation.validateHeight("175"))
    }

    @Test
    fun `peso volta ao campo sem casa decimal quando ela e zero`() {
        // Devolver "72.0" a quem digitou "72" parece correção de algo que estava certo.
        assertEquals("72", TrainingFormValidation.weightInput(72.0))
        assertEquals("72,5", TrainingFormValidation.weightInput(72.5))
        assertEquals("", TrainingFormValidation.weightInput(null))
    }
}
