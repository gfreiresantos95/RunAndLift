package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As especialidades do treinador, e o contrato que elas têm com o objetivo do aluno.
 *
 * O teste do espelho é o que importa aqui. **As cinco primeiras especialidades têm exatamente os
 * mesmos nomes dos cinco [TrainingGoal]**, e a busca por treinador depende disso para casar os dois
 * lados comparando campo com campo, sem tabela de tradução no meio. Renomear `WEIGHT_LOSS` de um
 * lado só compila, passa em toda tela e produz uma busca que não acha ninguém — em silêncio.
 */
class TrainerSpecialtyTest {

    @Test
    fun `toda especialidade de objetivo tem o nome do objetivo correspondente`() {
        val specialties = TrainerSpecialty.entries.map { it.name }

        TrainingGoal.entries.forEach {
            assertTrue("${it.name} deveria existir como especialidade de mesmo nome", it.name in specialties)
        }
    }

    @Test
    fun `as especialidades que sobram sao publicos, e nao objetivos`() {
        // Corrida, pós-reabilitação e terceira idade mudam a competência exigida, não a meta do
        // treino — é por isso que elas não têm par do lado do aluno, e não deveriam ganhar um.
        val goals = TrainingGoal.entries.map { it.name }.toSet()

        assertEquals(
            listOf("RUNNING", "REHAB_SUPPORT", "SENIORS"),
            TrainerSpecialty.entries.map { it.name }.filterNot { it in goals },
        )
    }

    @Test
    fun `ida e volta preserva a especialidade`() {
        TrainerSpecialty.entries.forEach {
            assertEquals(it, TrainerSpecialty.fromStored(it.name))
        }
    }

    @Test
    fun `valor desconhecido vira nulo, e nao excecao`() {
        // O documento pode trazer uma especialidade de uma versão futura do app, ou um campo
        // corrompido. Nenhum dos dois pode impedir alguém de abrir o perfil.
        assertNull(TrainerSpecialty.fromStored("CROSSFIT"))
        assertNull(TrainerSpecialty.fromStored(null))
        assertNull(TrainerSpecialty.fromStored(""))
    }
}
