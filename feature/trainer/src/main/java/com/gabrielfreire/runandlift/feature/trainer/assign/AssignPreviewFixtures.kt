package com.gabrielfreire.runandlift.feature.trainer.assign

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/** Exemplos dos previews da tela de atribuição. */

internal fun previewAssignState(): AssignUiState = AssignUiState(
    loading = false,
    program = Program(
        id = "p1",
        trainerId = "t1",
        name = "Treino ABC · Hipertrofia",
        goal = TrainingGoal.HYPERTROPHY,
        days = listOf(
            ProgramDay(
                label = "A",
                focus = "Peito e tríceps",
                exercises = listOf(
                    PrescribedExercise(
                        exerciseId = "supino",
                        exerciseName = "Supino Reto com Barra",
                        sets = 4,
                        minReps = 8,
                        maxReps = 12,
                    ),
                ),
            ),
        ),
    ),
    students = listOf(
        link(studentId = "a1", name = "Ana Ribeiro"),
        link(studentId = "a2", name = "Bruno Tavares"),
        // Sem nome: quem entrou pelo Google pode não ter um gravado, e a linha precisa aguentar.
        link(studentId = "a3", name = ""),
    ),
    // O primeiro já está com o programa — é o estado em que a linha tem duas ações em vez de uma.
    assignedIds = setOf("a1"),
)

internal fun previewAssignActions(): AssignActions = AssignActions(onAssign = {}, onRemove = {}, onRetry = {})

private fun link(studentId: String, name: String) = Link(
    trainerId = "t1",
    studentId = studentId,
    status = LinkStatus.ACTIVE,
    origin = LinkOrigin.INVITE_CODE,
    trainerName = "Marcos Vieira",
    studentName = name,
)
