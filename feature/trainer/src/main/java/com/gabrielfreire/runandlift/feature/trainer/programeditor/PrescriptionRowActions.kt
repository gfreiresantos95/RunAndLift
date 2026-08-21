package com.gabrielfreire.runandlift.feature.trainer.programeditor

/**
 * O que se faz com um exercício prescrito na lista do dia.
 *
 * Quatro lambdas sem argumento: a posição já está fechada por quem montou a linha, e passá-la de
 * novo a cada chamada é onde um índice trocado entra sem o compilador notar.
 */
internal data class PrescriptionRowActions(
    val onEdit: () -> Unit,
    val onMoveUp: () -> Unit,
    val onMoveDown: () -> Unit,
    val onRemove: () -> Unit,
)
