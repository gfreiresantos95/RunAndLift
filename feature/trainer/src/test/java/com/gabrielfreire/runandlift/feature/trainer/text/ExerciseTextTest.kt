package com.gabrielfreire.runandlift.feature.trainer.text

import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * A categoria do exercício apontando para o texto certo.
 *
 * `categoryRes` existe separado do `label()` composable justamente para poder ser afirmado aqui — o
 * filtro do catálogo monta a lista de chips **fora** de uma composição. O que se verifica é que
 * nenhuma categoria compartilha recurso com outra: um `when` copiado com a linha de cima esquecida
 * põe "Alongamento" no chip de "Pliometria", e a tela desenha isso sem reclamar.
 *
 * Não se afirma o texto em português — isso é `strings.xml`, e traduzi-lo de novo aqui seria manter
 * uma segunda cópia da tradução.
 */
class ExerciseTextTest {

    @Test
    fun `cada categoria aponta para um recurso proprio`() {
        val recursos = ExerciseCategory.entries.map { it.categoryRes }

        assertEquals("duas categorias no mesmo texto", recursos.size, recursos.distinct().size)
    }

    @Test
    fun `nenhuma categoria fica sem recurso`() {
        ExerciseCategory.entries.forEach {
            assertNotEquals("$it ficou sem texto", 0, it.categoryRes)
        }
    }
}
