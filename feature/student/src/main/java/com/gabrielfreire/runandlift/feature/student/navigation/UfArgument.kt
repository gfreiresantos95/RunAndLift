package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * A sigla do estado, viajando como argumento da lista de cidades.
 *
 * Não é opcional: uma lista de municípios sem estado seriam os 5.571 do país inteiro, que é
 * exatamente o que a tela existe para evitar. Por isso vai no caminho da rota, e não na consulta.
 */
internal fun ufArgument() = listOf(
    navArgument(StudentRoutes.UF_ARG) { type = NavType.StringType },
)
