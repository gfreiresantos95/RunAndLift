package com.gabrielfreire.runandlift.feature.trainer.programeditor

/**
 * O que o editor de dia faz.
 *
 * Os três últimos recebem a posição do exercício dentro do dia — o dia em si a tela já conhece, e
 * repeti-lo em cada chamada seria abrir espaço para passar o índice errado.
 */
internal data class DayEditorActions(
    val onInfoChange: (label: String, focus: String) -> Unit,
    val onAddExercise: () -> Unit,
    val onOpenExercise: (Int) -> Unit,
    val onRemoveExercise: (Int) -> Unit,
    val onMoveUp: (Int) -> Unit,
    val onMoveDown: (Int) -> Unit,
    val onRemoveDay: () -> Unit,
)
