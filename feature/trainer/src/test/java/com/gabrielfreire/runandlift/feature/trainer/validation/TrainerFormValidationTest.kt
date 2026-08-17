package com.gabrielfreire.runandlift.feature.trainer.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A régua da capacidade de atendimento.
 *
 * O que se afirma aqui é o que a mensagem promete: os limites pegam **erro de digitação**, e não
 * julgam prática. Vazio continua válido, porque o campo é opcional e o passo a passo deixa pular.
 */
class TrainerFormValidationTest {

    @Test
    fun `campo vazio nao e erro`() {
        assertNull(TrainerFormValidation.validateCapacity(""))
        assertNull(TrainerFormValidation.validateCapacity("   "))
    }

    @Test
    fun `numero dentro da faixa passa`() {
        assertNull(TrainerFormValidation.validateCapacity("1"))
        assertNull(TrainerFormValidation.validateCapacity("20"))
        assertNull(TrainerFormValidation.validateCapacity("300"))
    }

    @Test
    fun `zero e acima do limite nao passam`() {
        assertEquals(CapacityError.INVALID, TrainerFormValidation.validateCapacity("0"))
        assertEquals(CapacityError.INVALID, TrainerFormValidation.validateCapacity("301"))
    }

    @Test
    fun `texto que nao e numero nao passa`() {
        assertEquals(CapacityError.INVALID, TrainerFormValidation.validateCapacity("vinte"))
    }

    @Test
    fun `converte o texto do campo em numero`() {
        assertEquals(20, TrainerFormValidation.parseCapacity("20"))
        assertEquals(20, TrainerFormValidation.parseCapacity(" 20 "))
        assertNull(TrainerFormValidation.parseCapacity(""))
    }

    @Test
    fun `o que esta gravado volta para o campo`() {
        assertEquals("20", TrainerFormValidation.capacityInput(20))
        assertEquals("", TrainerFormValidation.capacityInput(null))
    }
}
