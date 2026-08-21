package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * Exemplos dos previews da montagem de treino.
 *
 * A regra ao mexer aqui: o exemplo é o **estado que costuma sair errado**, e não o feliz. Por isso o
 * programa tem um dia vazio e a prescrição mais pobre não tem carga, descanso nem observação — são
 * os casos em que a tela precisa decidir o que escrever, e um exemplo completo esconde todos eles.
 */

internal fun previewPrescriptions(): List<PrescribedExercise> = listOf(
    PrescribedExercise(
        exerciseId = "supino",
        exerciseName = "Supino Reto com Barra",
        sets = 4,
        minReps = 8,
        maxReps = 12,
        loadKg = 60.0,
        restSeconds = 90,
        notes = "Desça até encostar de leve e suba controlando.",
    ),
    PrescribedExercise(
        exerciseId = "crucifixo",
        exerciseName = "Crossover na Polia",
        sets = 3,
        minReps = 12,
        maxReps = 15,
        loadKg = 22.5,
        restSeconds = 60,
    ),
    PrescribedExercise(
        exerciseId = "triceps",
        exerciseName = "Tríceps na Polia",
        sets = 3,
        minReps = 10,
        maxReps = 10,
        restSeconds = 45,
    ),
)

/** A prescrição mínima: só séries e repetições, que é como um exercício nasce ao ser escolhido. */
internal fun previewBarePrescription(): PrescribedExercise = PrescribedExercise(
    exerciseId = "remada",
    exerciseName = "Remada Curvada",
    sets = 3,
    minReps = 8,
    maxReps = 12,
)

internal fun previewDay(): ProgramDay = ProgramDay(
    label = "A",
    focus = "Peito e tríceps",
    exercises = previewPrescriptions(),
)

internal fun previewEditorState(): ProgramEditorUiState = ProgramEditorUiState(
    program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC · Hipertrofia",
        goal = TrainingGoal.HYPERTROPHY,
        notes = "Progredir carga a cada duas semanas.",
        // Um dia montado e um vazio: o vazio é o que faz o aviso de incompleto aparecer, e é o
        // único jeito de conferir se ele se distingue do resto da tela.
        days = listOf(previewDay(), ProgramDay(label = "B", focus = "Costas e bíceps")),
    ),
)

internal fun previewEditorActions(): ProgramEditorActions = ProgramEditorActions(
    onNameChange = {},
    onGoalChange = {},
    onNotesChange = {},
    onAddDay = {},
    onOpenDay = {},
    onRemoveDay = {},
    onSave = {},
    onAssign = {},
)

internal fun previewDayActions(): DayEditorActions = DayEditorActions(
    onInfoChange = { _, _ -> },
    onAddExercise = {},
    onOpenExercise = {},
    onRemoveExercise = {},
    onMoveUp = {},
    onMoveDown = {},
    onRemoveDay = {},
)

internal fun previewRowActions(): PrescriptionRowActions = PrescriptionRowActions(
    onEdit = {},
    onMoveUp = {},
    onMoveDown = {},
    onRemove = {},
)
