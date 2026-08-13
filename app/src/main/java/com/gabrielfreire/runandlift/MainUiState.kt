package com.gabrielfreire.runandlift

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes

/**
 * O que a abertura precisa saber antes de compor qualquer tela.
 *
 * [startDestination] já vem resolvido, e não é recalculado durante a composição: é o que impede o
 * app de abrir no login e trocar para a home um frame depois.
 */
data class MainUiState(
    /** Enquanto for `false`, a splash permanece na tela. */
    val ready: Boolean = false,
    val startDestination: String = AuthRoutes.GRAPH,
    val activeRole: ActiveRole? = null,
    val canSwitchRole: Boolean = false,
)
