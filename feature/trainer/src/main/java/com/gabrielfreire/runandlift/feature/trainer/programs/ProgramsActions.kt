package com.gabrielfreire.runandlift.feature.trainer.programs

import com.gabrielfreire.runandlift.data.model.Program

/**
 * O que a aba de treinos faz.
 *
 * Agrupadas porque a tela já recebe estado e abas, e três funções soltas na assinatura seriam três
 * lugares para errar a ordem — é a mesma razão de `StudentsActions`.
 */
internal data class ProgramsActions(
    val onCreate: () -> Unit,
    val onOpen: (Program) -> Unit,
    val onDelete: (Program) -> Unit,
    val onRetry: () -> Unit,
)
