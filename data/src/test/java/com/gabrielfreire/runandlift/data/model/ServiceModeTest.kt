package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As modalidades de atendimento, ida e volta do banco.
 *
 * O segundo teste guarda uma decisão que some fácil: **não existe "híbrido"**. Híbrido é presencial
 * e online marcados juntos, e uma terceira opção que significasse "as duas anteriores" daria à
 * busca dois jeitos de dizer a mesma coisa — com metade das pessoas marcando as três.
 */
class ServiceModeTest {

    @Test
    fun `ida e volta preserva a modalidade`() {
        ServiceMode.entries.forEach {
            assertEquals(it, ServiceMode.fromStored(it.name))
        }
    }

    @Test
    fun `sao tres, e nenhuma delas significa as outras duas`() {
        assertEquals(listOf("IN_PERSON", "ONLINE", "HOME_VISIT"), ServiceMode.entries.map { it.name })
        assertTrue(
            "híbrido é presencial e online marcados juntos, e não uma opção",
            ServiceMode.entries.none { it.name.contains("HYBRID") },
        )
    }

    @Test
    fun `valor desconhecido vira nulo, e nao excecao`() {
        assertNull(ServiceMode.fromStored("HYBRID"))
        assertNull(ServiceMode.fromStored(null))
    }
}
