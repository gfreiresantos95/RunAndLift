package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gabrielfreire.runandlift.data.model.ActiveRole

/**
 * O perfil escolhido nas boas-vindas, viajando como argumento de rota.
 *
 * A declaração e a leitura ficam juntas porque são os dois lados da mesma coisa: quem registra o
 * argumento sem nulidade e quem o lê esperando `null` produzem um par que só falha em tempo de
 * execução.
 *
 * É **opcional e com padrão nulo** — as três telas que o recebem funcionam sem ele, e é isso que
 * permite alcançá-las por deep link ou por uma sessão antiga sem passar pela abertura.
 */
internal fun roleArgument() = listOf(
    navArgument(AuthRoutes.ROLE_ARG) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    },
)

internal fun NavBackStackEntry.role(): ActiveRole? = ActiveRole.fromStorage(arguments?.getString(AuthRoutes.ROLE_ARG))
