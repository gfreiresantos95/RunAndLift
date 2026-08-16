package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A faixa de tempo de atuação, ida e volta do banco.
 *
 * O nome do enum **é** o valor gravado em `trainerProfiles/{uid}`: renomear uma faixa compila e faz
 * o perfil de quem já respondeu voltar em branco, como se ele nunca tivesse respondido.
 */
class TrainerExperienceTest {

    @Test
    fun `ida e volta preserva a faixa`() {
        TrainerExperience.entries.forEach {
            assertEquals(it, TrainerExperience.fromStored(it.name))
        }
    }

    @Test
    fun `as faixas vao da menor para a maior`() {
        // A ordem do enum é a ordem dos chips na tela, e ela precisa ser crescente: uma lista de
        // faixas fora de ordem faz a pessoa reler as quatro para achar a sua.
        assertEquals(
            listOf("UP_TO_TWO_YEARS", "TWO_TO_FIVE_YEARS", "FIVE_TO_TEN_YEARS", "OVER_TEN_YEARS"),
            TrainerExperience.entries.map { it.name },
        )
    }

    @Test
    fun `valor desconhecido vira nulo, e nao excecao`() {
        assertNull(TrainerExperience.fromStored("VINTE_ANOS"))
        assertNull(TrainerExperience.fromStored(null))
    }
}
