package com.gabrielfreire.runandlift.data.remote.exercise

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O que entra no catálogo, e o que é descartado sem levar o resto junto.
 *
 * A regra tem duas metades e as duas importam. Sem nome, o documento não entra — não há como
 * mostrá-lo numa lista, e uma linha em branco no catálogo é pior que uma linha a menos. Com qualquer
 * outro campo estranho, ele entra assim mesmo: **um registro mal formado não pode derrubar a
 * sincronização inteira e deixar o app sem exercício nenhum**, que é o oposto de offline funcionando.
 */
class ExerciseDocumentTest {

    @Test
    fun `o documento completo vira exercicio`() {
        val exercise = ExerciseDocument.exercise(
            id = "supino-reto",
            name = "Supino reto",
            muscleGroups = listOf("peito", "tríceps"),
            equipment = "barra",
        )

        assertEquals("supino-reto", exercise?.id)
        assertEquals(listOf("peito", "tríceps"), exercise?.muscleGroups)
        assertEquals("barra", exercise?.equipment)
    }

    @Test
    fun `sem nome nao ha exercicio`() {
        assertNull(ExerciseDocument.exercise(id = "x", name = null))
    }

    @Test
    fun `o que falta e opcional vira ausencia, e o exercicio entra`() {
        val exercise = ExerciseDocument.exercise(id = "agachamento", name = "Agachamento")

        assertEquals(emptyList<String>(), exercise?.muscleGroups)
        assertNull(exercise?.equipment)
        assertNull(exercise?.mediaUrl)
    }

    @Test
    fun `grupo muscular que nao e texto some sem levar os outros`() {
        val exercise = ExerciseDocument.exercise(
            id = "remada",
            name = "Remada",
            muscleGroups = listOf("costas", 42, null, "bíceps"),
        )

        assertEquals(listOf("costas", "bíceps"), exercise?.muscleGroups)
    }

    @Test
    fun `campo de lista com tipo errado vira lista vazia, e nao excecao`() {
        val exercise = ExerciseDocument.exercise(id = "remada", name = "Remada", muscleGroups = "costas")

        assertEquals(emptyList<String>(), exercise?.muscleGroups)
    }

    @Test
    fun `o catalogo global nao tem dono`() {
        // Esta consulta traz só `ownerId == null`; gravar um dono aqui poria um exercício de
        // treinador dentro do catálogo de todo mundo.
        assertNull(ExerciseDocument.exercise(id = "supino", name = "Supino")?.ownerId)
    }
}
