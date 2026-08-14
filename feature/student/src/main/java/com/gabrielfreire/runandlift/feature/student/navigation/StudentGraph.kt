package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.feature.student.home.StudentHomeDestination
import com.gabrielfreire.runandlift.feature.student.menu.StudentMenuDestination
import com.gabrielfreire.runandlift.feature.student.onboarding.OnboardingDestination
import com.gabrielfreire.runandlift.feature.student.profile.StudentProfileDestination
import com.gabrielfreire.runandlift.feature.student.workouts.StudentWorkoutsDestination

/**
 * Grafo do aluno: as três abas, mais o onboarding e a edição de perfil.
 *
 * As três abas são **irmãs dentro do grafo**, e não um grafo aninhado por aba: aba não é fluxo.
 * O onboarding e o perfil ficam no mesmo nível, mas fora da barra inferior — são tarefas com começo
 * e fim, e uma barra no rodapé ofereceria saída lateral no meio delas.
 *
 * O onboarding **substitui a home na pilha** ao terminar: voltar para um passo a passo já concluído
 * não faz sentido, e o botão voltar do aparelho o levaria de volta ao primeiro passo.
 *
 * Este arquivo é só o mapa. A ligação de cada destino com o seu ViewModel mora no pacote da tela.
 *
 * Os repositórios chegam reunidos em [StudentDependencies], e não um a um: o onboarding trouxe o
 * terceiro, e a assinatura desta função — mais a de quem a chama — mudaria a cada tela nova que
 * precisasse de mais um. Assim o que muda é o conteúdo daquela classe.
 *
 * @param onSignedOut para onde ir quando a sessão terminar. Quem sabe é `:app`.
 * @param onSwitchRole `null` quando a conta não tem o papel de treinador.
 */
fun NavGraphBuilder.studentGraph(
    navController: NavHostController,
    dependencies: StudentDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
) {
    navigation(startDestination = StudentRoutes.HOME, route = StudentRoutes.GRAPH) {
        composable(StudentRoutes.HOME) {
            StudentHomeDestination(
                navController = navController,
                dependencies = dependencies,
                onOpenProfile = { navController.navigate(StudentRoutes.PROFILE) },
            )
        }
        composable(StudentRoutes.WORKOUTS) {
            StudentWorkoutsDestination(navController = navController)
        }
        composable(StudentRoutes.MENU) {
            StudentMenuDestination(
                navController = navController,
                dependencies = dependencies,
                onSignedOut = onSignedOut,
                onSwitchRole = onSwitchRole,
                onOpenProfile = { navController.navigate(StudentRoutes.PROFILE) },
            )
        }
        composable(StudentRoutes.ONBOARDING) {
            OnboardingDestination(
                dependencies = dependencies,
                onFinished = {
                    navController.navigate(StudentRoutes.HOME) {
                        popUpTo(StudentRoutes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(StudentRoutes.PROFILE) {
            StudentProfileDestination(
                dependencies = dependencies,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
