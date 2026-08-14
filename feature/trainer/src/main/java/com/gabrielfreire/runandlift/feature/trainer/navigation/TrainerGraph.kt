package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.feature.trainer.home.TrainerHomeDestination
import com.gabrielfreire.runandlift.feature.trainer.menu.TrainerMenuDestination
import com.gabrielfreire.runandlift.feature.trainer.workouts.TrainerWorkoutsDestination

/**
 * Grafo do treinador: início, treinos e menu.
 *
 * Irmão do grafo do aluno e sem nenhuma rota em comum com ele — o que antes era disciplina de
 * nomenclatura agora é fronteira de módulo: este arquivo não consegue nomear uma tela de aluno nem
 * por engano, porque não a enxerga.
 *
 * @param onSignedOut para onde ir quando a sessão terminar. Quem sabe é `:app`.
 * @param onSwitchRole `null` quando a conta não tem o papel de aluno.
 */
fun NavGraphBuilder.trainerGraph(
    navController: NavHostController,
    dependencies: TrainerDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
) {
    navigation(startDestination = TrainerRoutes.HOME, route = TrainerRoutes.GRAPH) {
        composable(TrainerRoutes.HOME) {
            TrainerHomeDestination(navController = navController, dependencies = dependencies)
        }
        composable(TrainerRoutes.WORKOUTS) {
            TrainerWorkoutsDestination(navController = navController)
        }
        composable(TrainerRoutes.MENU) {
            TrainerMenuDestination(
                navController = navController,
                dependencies = dependencies,
                onSignedOut = onSignedOut,
                onSwitchRole = onSwitchRole,
            )
        }
    }
}
