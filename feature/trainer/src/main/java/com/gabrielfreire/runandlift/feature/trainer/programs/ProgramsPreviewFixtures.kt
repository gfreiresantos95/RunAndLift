package com.gabrielfreire.runandlift.feature.trainer.programs

import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * Programas de exemplo dos previews da aba de treinos.
 *
 * Arquivo à parte, como os outros `*PreviewFixtures`: são dados de conferência visual, não fazem
 * parte da tela, e ficam fora da medição de cobertura por nome.
 */

internal fun previewPrograms(): List<Program> = listOf(
    Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC · Hipertrofia",
        goal = TrainingGoal.HYPERTROPHY,
        days = listOf(
            ProgramDay(label = "A", focus = "Peito e tríceps", exercises = previewExercises(SPLIT_DAY_SIZE)),
            ProgramDay(label = "B", focus = "Costas e bíceps", exercises = previewExercises(SPLIT_DAY_SIZE)),
            ProgramDay(label = "C", focus = "Pernas e ombros", exercises = previewExercises(SPLIT_DAY_SIZE)),
        ),
    ),
    Program(
        id = "p2",
        trainerId = "t1",
        name = "Full body · 2x por semana",
        goal = TrainingGoal.HEALTH,
        days = listOf(
            ProgramDay(label = "A", focus = "Corpo inteiro", exercises = previewExercises(FULL_BODY_SIZE)),
        ),
    ),
)

/** Seis exercícios por dia é o tamanho comum de um ABC; oito, o de um treino de corpo inteiro. */
private const val SPLIT_DAY_SIZE = 6
private const val FULL_BODY_SIZE = 8

/**
 * O programa pela metade, que é o estado que a lista precisa saber mostrar.
 *
 * Um dia sem exercício nenhum: é o que impede a atribuição, e é o caso que um exemplo só de
 * programas prontos nunca revelaria.
 */
internal fun previewIncompleteProgram(): Program = Program(
    id = "p3",
    trainerId = "t1",
    name = "Rascunho de força",
    days = listOf(ProgramDay(label = "A")),
)

internal fun previewProgramsState(): ProgramsUiState =
    ProgramsUiState(loading = false, programs = previewPrograms() + previewIncompleteProgram())

internal fun previewProgramsActions(): ProgramsActions =
    ProgramsActions(onCreate = {}, onOpen = {}, onDelete = {}, onRetry = {})

private fun previewExercises(count: Int): List<PrescribedExercise> = List(count) { index ->
    PrescribedExercise(
        exerciseId = "e$index",
        exerciseName = "Exercício ${index + 1}",
        sets = 4,
        minReps = 8,
        maxReps = 12,
    )
}
