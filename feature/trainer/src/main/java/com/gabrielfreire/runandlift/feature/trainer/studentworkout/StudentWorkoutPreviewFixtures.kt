package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * Exemplos dos previews do treino de um aluno.
 *
 * A regra ao mexer aqui é a das outras fixtures: o exemplo é o **estado que costuma sair errado**, e
 * não o feliz. Por isso um dos exercícios não tem carga, descanso nem observação, e um dos dias não
 * tem foco escrito — são os casos em que a tela precisa decidir o que escrever, e um exemplo
 * completo esconde todos eles.
 */

internal fun previewAssignedExercises(): List<PrescribedExercise> = listOf(
    PrescribedExercise(
        exerciseId = "supino",
        exerciseName = "Supino Reto com Barra",
        sets = 4,
        minReps = 8,
        maxReps = 12,
        loadKg = 62.5,
        restSeconds = 90,
        notes = "Desça até encostar de leve e suba controlando.",
    ),
    PrescribedExercise(
        exerciseId = "crossover",
        exerciseName = "Crossover na Polia",
        sets = 3,
        minReps = 12,
        maxReps = 12,
        loadKg = 22.0,
        restSeconds = 60,
    ),
    // Sem carga, sem descanso e sem observação — o mínimo que uma prescrição pode ser.
    PrescribedExercise(
        exerciseId = "remada",
        exerciseName = "Remada Curvada",
        sets = 3,
        minReps = 8,
        maxReps = 12,
    ),
)

internal fun previewStudentWorkoutState(): StudentWorkoutUiState = StudentWorkoutUiState(
    loading = false,
    assignment = Assignment(
        trainerId = "t1",
        studentId = "a1",
        studentName = "Ana Ribeiro",
        programId = "p1",
        programName = "Treino ABC · Hipertrofia",
        goal = TrainingGoal.HYPERTROPHY,
        notes = "Progredir carga a cada duas semanas.",
        days = listOf(
            ProgramDay(label = "A", focus = "Peito e tríceps", exercises = previewAssignedExercises()),
            // Sem foco escrito: é onde se confere que o cabeçalho não inventa um texto no lugar.
            ProgramDay(label = "B", exercises = previewAssignedExercises().take(n = 2)),
        ),
    ),
)

internal fun previewStudentWorkoutActions(): StudentWorkoutActions =
    StudentWorkoutActions(onOpenPrograms = {}, onRetry = {})
