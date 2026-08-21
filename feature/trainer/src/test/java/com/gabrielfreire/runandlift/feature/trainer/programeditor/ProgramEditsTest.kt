package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * As mudanças que se faz num programa enquanto ele é montado.
 *
 * **O que estes testes existem para garantir é que índice fora da lista não derruba nada.** É o erro
 * mais provável da montagem inteira: o editor de dia recebe a posição por argumento de navegação, a
 * prescrição recebe duas, e as duas continuam valendo depois de o usuário remover alguma coisa — o
 * processo pode até ser recriado no meio, com a rota antiga e o programa novo. Um `IndexOutOfBounds`
 * aqui é uma tela que fecha sozinha levando junto um programa que ninguém tinha salvado.
 */
class ProgramEditsTest {

    private val program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC",
        days = listOf(
            ProgramDay(label = "A", focus = "Peito", exercises = listOf(prescription("supino"), prescription("fly"))),
            ProgramDay(label = "B"),
        ),
    )

    @Test
    fun `dia novo recebe a proxima letra do alfabeto`() {
        val added = program.withDayAdded()

        assertEquals("C", added.days.last().label)
    }

    @Test
    fun `passado o Z a contagem vira numero`() {
        val many = program.copy(days = List(26) { ProgramDay(label = "x") })

        assertEquals("27", many.withDayAdded().days.last().label)
    }

    @Test
    fun `remover dia tira so o dia pedido`() {
        val removed = program.withDayRemoved(dayIndex = 0)

        assertEquals(1, removed.days.size)
        assertEquals("B", removed.days.first().label)
    }

    @Test
    fun `remover dia que nao existe devolve o programa intacto`() {
        assertEquals(program, program.withDayRemoved(dayIndex = 9))
        assertEquals(program, program.withDayRemoved(dayIndex = -1))
    }

    @Test
    fun `foco em branco vira ausencia, e nao texto vazio`() {
        val changed = program.withDayInfo(dayIndex = 0, label = "A", focus = "   ")

        assertNull(changed.days.first().focus)
    }

    @Test
    fun `exercicio entra no fim do dia com a prescricao padrao`() {
        val added = program.withExerciseAdded(dayIndex = 1, exercise = exercise("agachamento"))
        val last = added.days[1].exercises.last()

        assertEquals("agachamento", last.exerciseId)
        assertEquals("Agachamento", last.exerciseName)
        assertEquals(PrescribedExercise.DEFAULT_SETS, last.sets)
        assertEquals(PrescribedExercise.DEFAULT_MIN_REPS, last.minReps)
        assertEquals(PrescribedExercise.DEFAULT_MAX_REPS, last.maxReps)
    }

    @Test
    fun `o mesmo exercicio pode entrar duas vezes no mesmo dia`() {
        val twice = program
            .withExerciseAdded(dayIndex = 1, exercise = exercise("supino"))
            .withExerciseAdded(dayIndex = 1, exercise = exercise("supino"))

        assertEquals(
            "supino duas vezes com cargas diferentes é prescrição comum",
            2,
            twice.days[1].exercises.size,
        )
    }

    @Test
    fun `adicionar em dia que nao existe nao faz nada`() {
        assertEquals(program, program.withExerciseAdded(dayIndex = 7, exercise = exercise("x")))
    }

    @Test
    fun `remover exercicio tira so o pedido`() {
        val removed = program.withExerciseRemoved(dayIndex = 0, exerciseIndex = 0)

        assertEquals(1, removed.days.first().exercises.size)
        assertEquals("fly", removed.days.first().exercises.first().exerciseId)
    }

    @Test
    fun `remover exercicio que nao existe devolve o programa intacto`() {
        assertEquals(program, program.withExerciseRemoved(dayIndex = 0, exerciseIndex = 9))
        assertEquals(program, program.withExerciseRemoved(dayIndex = 9, exerciseIndex = 0))
    }

    @Test
    fun `mover troca a ordem de execucao`() {
        val moved = program.withExerciseMoved(dayIndex = 0, from = 1, to = 0)

        assertEquals(listOf("fly", "supino"), moved.days.first().exercises.map { it.exerciseId })
    }

    @Test
    fun `mover para fora da lista nao faz nada`() {
        // É o que acontece ao tocar "subir" no primeiro item, e não pode virar exceção.
        assertEquals(program, program.withExerciseMoved(dayIndex = 0, from = 0, to = -1))
        assertEquals(program, program.withExerciseMoved(dayIndex = 0, from = 1, to = 2))
    }

    @Test
    fun `prescricao substitui so o exercicio pedido`() {
        val updated = program.withPrescription(
            dayIndex = 0,
            exerciseIndex = 1,
            prescription = prescription("fly").copy(sets = 5, loadKg = 20.0),
        )

        assertEquals(5, updated.days.first().exercises[1].sets)
        assertEquals(
            "o vizinho não pode mudar junto",
            PrescribedExercise.DEFAULT_SETS,
            updated.days.first().exercises[0].sets,
        )
    }

    private fun prescription(id: String) = PrescribedExercise(
        exerciseId = id,
        exerciseName = id.replaceFirstChar(Char::uppercase),
        sets = PrescribedExercise.DEFAULT_SETS,
        minReps = PrescribedExercise.DEFAULT_MIN_REPS,
        maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
    )

    private fun exercise(id: String) = Exercise(
        id = id,
        name = id.replaceFirstChar(Char::uppercase),
        muscleGroups = listOf("Peitoral"),
        equipment = "Barra",
        instructions = listOf("Execute."),
    )
}
