package com.gabrielfreire.runandlift.feature.auth.onboarding

import com.gabrielfreire.runandlift.data.model.ActiveRole

/**
 * Estado da escolha de papel feita **depois** de autenticar.
 *
 * [confirmedRole] é o desfecho, e não um espelho de [selected]: só é preenchido quando a gravação
 * em `users/{uid}` deu certo. Navegar a partir da seleção mandaria para o app alguém cujo papel não
 * chegou a ser gravado.
 */
internal data class RoleSelectionUiState(
    val selected: ActiveRole? = null,
    val submitting: Boolean = false,
    val failed: Boolean = false,
    val confirmedRole: ActiveRole? = null,
)
