package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.feature.trainer.account.AccountDestination
import com.gabrielfreire.runandlift.feature.trainer.home.TrainerHomeDestination
import com.gabrielfreire.runandlift.feature.trainer.location.LocationPickerDestination
import com.gabrielfreire.runandlift.feature.trainer.menu.TrainerMenuDestination
import com.gabrielfreire.runandlift.feature.trainer.onboarding.OnboardingDestination
import com.gabrielfreire.runandlift.feature.trainer.profile.TrainerProfileDestination
import com.gabrielfreire.runandlift.feature.trainer.workouts.TrainerWorkoutsDestination

/**
 * Grafo do treinador: as três abas, mais o passo a passo, o perfil profissional e os dados
 * cadastrais.
 *
 * Irmão do grafo do aluno e sem nenhuma rota em comum com ele — o que antes era disciplina de
 * nomenclatura agora é fronteira de módulo: este arquivo não consegue nomear uma tela de aluno nem
 * por engano, porque não a enxerga.
 *
 * O passo a passo **substitui a home na pilha** ao terminar: voltar para um fluxo já concluído não
 * faz sentido, e o botão voltar do aparelho o levaria de volta ao primeiro passo.
 *
 * Este arquivo é só o mapa. A ligação de cada destino com o seu ViewModel mora no pacote da tela.
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
            TrainerHomeDestination(
                navController = navController,
                dependencies = dependencies,
                onOpenProfile = { navController.navigate(TrainerRoutes.PROFILE) },
            )
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
                onOpen = { route -> navController.navigate(route) },
            )
        }
        composable(TrainerRoutes.ACCOUNT) { entry ->
            AccountDestination(
                navController = navController,
                entry = entry,
                dependencies = dependencies,
                // Salvar volta levando o recibo; a seta volta sem ele, porque nada foi gravado.
                onSaved = { navController.popWithSavedResult() },
                onBack = { navController.popBackStack() },
            )
        }

        // As duas listas de localidade, abertas por "Meus dados". Ficam no mesmo grafo porque a
        // escolha volta pela entrada anterior da pilha — quem as abre é uma tela daqui.
        composable(TrainerRoutes.STATE_PICKER) {
            LocationPickerDestination(navController = navController, dependencies = dependencies, uf = null)
        }
        composable(route = TrainerRoutes.CITY_PICKER_PATTERN, arguments = ufArgument()) { entry ->
            LocationPickerDestination(
                navController = navController,
                dependencies = dependencies,
                uf = entry.arguments?.getString(TrainerRoutes.UF_ARG),
            )
        }
        composable(TrainerRoutes.ONBOARDING) {
            OnboardingDestination(
                dependencies = dependencies,
                onFinished = {
                    navController.navigate(TrainerRoutes.HOME) {
                        popUpTo(TrainerRoutes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(TrainerRoutes.PROFILE) {
            TrainerProfileDestination(
                dependencies = dependencies,
                onSaved = { navController.popWithSavedResult() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
