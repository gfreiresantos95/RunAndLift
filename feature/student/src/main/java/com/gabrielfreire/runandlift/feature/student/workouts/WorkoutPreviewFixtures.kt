package com.gabrielfreire.runandlift.feature.student.workouts

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/*
 * Dados de exemplo dos @Preview desta aba.
 *
 * Existem para desenhar a tela e não rodam em produção — é por isso que `*PreviewFixtures*` está
 * fora da medição de cobertura. As frases são de treino real, e não "Lorem ipsum": um card de
 * exercício com texto falso não mostra o que acontece quando o nome do movimento tem quatro
 * palavras, que é o caso comum do catálogo importado.
 */

/** Callbacks inertes: a tela desenha, e nada acontece ao tocar. */
internal fun previewWorkoutsActions() = StudentWorkoutsActions(onOpenDay = {}, onRetry = {})

/** A prescrição cheia: faixa de repetições, carga quebrada e recado do treinador. */
internal fun previewFullPrescription() = PrescribedExercise(
    exerciseId = "supino-reto-barra",
    exerciseName = "Supino reto com barra",
    sets = 4,
    minReps = 8,
    maxReps = 12,
    loadKg = 62.5,
    restSeconds = 90,
    notes = "Desce devagar, não trave o cotovelo em cima.",
)

/** O mínimo que uma prescrição pode ser: séries, repetição fixa e nada mais. */
internal fun previewBarePrescription() = PrescribedExercise(
    exerciseId = "prancha",
    exerciseName = "Prancha",
    sets = 3,
    minReps = 30,
    maxReps = 30,
)

/** Um dia com três exercícios, que é o tamanho em que a lista já mostra o seu ritmo. */
internal fun previewDay() = ProgramDay(
    label = "A",
    focus = "Peito e tríceps",
    exercises = listOf(
        previewFullPrescription(),
        PrescribedExercise(
            exerciseId = "supino-inclinado-halter",
            exerciseName = "Supino inclinado com halteres",
            sets = 3,
            minReps = 10,
            maxReps = 12,
            loadKg = 22.0,
            restSeconds = 60,
        ),
        previewBarePrescription(),
    ),
)

/** O treino como ele chega: três dias, um deles ainda sem foco escrito. */
internal fun previewAssignment() = Assignment(
    trainerId = "t1",
    studentId = "a1",
    studentName = "Ana Souza",
    programId = "p1",
    programName = "Full body iniciante",
    goal = TrainingGoal.HYPERTROPHY,
    notes = "Aquecer 10 minutos de esteira antes de começar.",
    days = listOf(
        previewDay(),
        ProgramDay(label = "B", focus = "Costas e bíceps", exercises = listOf(previewFullPrescription())),
        ProgramDay(label = "C", exercises = listOf(previewBarePrescription(), previewFullPrescription())),
    ),
    status = AssignmentStatus.ACTIVE,
)
