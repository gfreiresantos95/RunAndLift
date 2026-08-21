package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.TrainingLevel
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
 *
 * Os testes de enum guardam a outra metade do contrato, a que se firmou com o importador
 * (`tools/catalog/`): valor que ele grava tem que ser valor que isto reconhece. Um `ADVANCED` virado
 * `EXPERT` de um lado só não quebra build nenhum — deixa o filtro de nível vazio, e ninguém percebe
 * até um treinador procurar por avançado e não achar nada.
 */
class ExerciseDocumentTest {

    @Test
    fun `o documento completo vira exercicio`() {
        val exercise = ExerciseDocument.exercise(
            id = "supino-reto",
            name = "Supino reto",
            fields = ExerciseDocument.Fields(
                muscleGroups = listOf("Peitoral", "Tríceps"),
                secondaryMuscleGroups = listOf("Ombros"),
                equipment = "Barra",
                instructions = listOf("Deite no banco.", "Empurre a barra."),
                level = "INTERMEDIATE",
                mechanic = "COMPOUND",
                force = "PUSH",
                category = "STRENGTH",
            ),
        )

        assertEquals("supino-reto", exercise?.id)
        assertEquals(listOf("Peitoral", "Tríceps"), exercise?.muscleGroups)
        assertEquals(listOf("Ombros"), exercise?.secondaryMuscleGroups)
        assertEquals("Barra", exercise?.equipment)
        assertEquals(listOf("Deite no banco.", "Empurre a barra."), exercise?.instructions)
        assertEquals(TrainingLevel.INTERMEDIATE, exercise?.level)
        assertEquals(ExerciseMechanic.COMPOUND, exercise?.mechanic)
        assertEquals(ExerciseForce.PUSH, exercise?.force)
        assertEquals(ExerciseCategory.STRENGTH, exercise?.category)
    }

    @Test
    fun `sem nome nao ha exercicio`() {
        assertNull(ExerciseDocument.exercise(id = "x", name = null))
    }

    @Test
    fun `o que falta e opcional vira ausencia, e o exercicio entra`() {
        val exercise = ExerciseDocument.exercise(id = "agachamento", name = "Agachamento")

        assertEquals(emptyList<String>(), exercise?.muscleGroups)
        assertEquals(emptyList<String>(), exercise?.instructions)
        assertNull(exercise?.equipment)
        assertNull(exercise?.mediaUrl)
        assertNull(exercise?.level)
        assertNull(exercise?.mechanic)
        assertNull(exercise?.force)
    }

    @Test
    fun `categoria ausente cai em musculacao, e nao sai das listas`() {
        val exercise = ExerciseDocument.exercise(id = "agachamento", name = "Agachamento")

        assertEquals(
            "sem categoria o exercício sumiria de todo filtro, que é pior que cair na família errada",
            ExerciseCategory.STRENGTH,
            exercise?.category,
        )
    }

    @Test
    fun `enum desconhecido nao derruba o exercicio`() {
        val exercise = ExerciseDocument.exercise(
            id = "remada",
            name = "Remada",
            fields = ExerciseDocument.Fields(level = "MASTER", mechanic = "HYBRID", force = "TWIST"),
        )

        assertEquals("Remada", exercise?.name)
        assertNull(exercise?.level)
        assertNull(exercise?.mechanic)
        assertNull(exercise?.force)
    }

    @Test
    fun `grupo muscular que nao e texto some sem levar os outros`() {
        val exercise = ExerciseDocument.exercise(
            id = "remada",
            name = "Remada",
            fields = ExerciseDocument.Fields(muscleGroups = listOf("costas", 42, null, "bíceps")),
        )

        assertEquals(listOf("costas", "bíceps"), exercise?.muscleGroups)
    }

    @Test
    fun `campo de lista com tipo errado vira lista vazia, e nao excecao`() {
        val exercise = ExerciseDocument.exercise(
            id = "remada",
            name = "Remada",
            fields = ExerciseDocument.Fields(muscleGroups = "costas", instructions = "desça a barra"),
        )

        assertEquals(emptyList<String>(), exercise?.muscleGroups)
        assertEquals(emptyList<String>(), exercise?.instructions)
    }

    @Test
    fun `o catalogo global nao tem dono`() {
        // Esta consulta traz só `ownerId == null`; gravar um dono aqui poria um exercício de
        // treinador dentro do catálogo de todo mundo.
        assertNull(ExerciseDocument.exercise(id = "supino", name = "Supino")?.ownerId)
    }
}
