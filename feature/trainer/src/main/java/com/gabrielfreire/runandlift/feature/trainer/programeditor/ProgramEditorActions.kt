package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * O que o editor de programa faz.
 *
 * Agrupadas porque são sete, e sete lambdas soltas numa assinatura são sete oportunidades de trocar
 * a ordem de duas que têm o mesmo tipo — é a razão de `StudentsActions` e de `TrainerFormActions`.
 */
internal data class ProgramEditorActions(
    val onNameChange: (String) -> Unit,
    val onGoalChange: (TrainingGoal?) -> Unit,
    val onNotesChange: (String) -> Unit,
    val onAddDay: () -> Unit,
    val onOpenDay: (Int) -> Unit,
    val onRemoveDay: (Int) -> Unit,
    val onSave: () -> Unit,
    val onAssign: () -> Unit,
)
