package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * A sigla do estado, viajando como argumento da lista de cidades.
 *
 * Ao contrário do perfil em [roleArgument], este argumento **não é opcional**: uma lista de
 * municípios sem estado seriam os 5.571 do país inteiro, que é exatamente o que a tela existe para
 * evitar. Por isso ele vai no caminho da rota, e não na consulta.
 */
internal fun ufArgument() = listOf(
    navArgument(AuthRoutes.UF_ARG) { type = NavType.StringType },
)
