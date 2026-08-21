package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * A posição do dia dentro da prescrição, viajando como argumento.
 *
 * `IntType` e não `StringType` porque é índice de lista, e deixar o Navigation converter é o que
 * evita um `toIntOrNull()` no destino — uma rota com letra no lugar do número simplesmente não casa
 * com o padrão, em vez de casar e abrir a tela com um dia nulo.
 */
internal fun workoutDayArgument() = listOf(
    navArgument(StudentRoutes.DAY_INDEX_ARG) { type = NavType.IntType },
)
