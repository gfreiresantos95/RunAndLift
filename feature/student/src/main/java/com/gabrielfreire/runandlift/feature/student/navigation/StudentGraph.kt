package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.student.home.StudentHomeDestination
import com.gabrielfreire.runandlift.feature.student.menu.StudentMenuDestination
import com.gabrielfreire.runandlift.feature.student.workouts.StudentWorkoutsDestination

/**
 * Grafo do aluno: início, treinos e menu.
 *
 * As três rotas são **irmãs dentro do grafo**, e não um grafo aninhado por aba. Aba não é fluxo:
 * uma pilha por aba resolveria um problema que não existe enquanto nenhuma delas tem tela filha, e
 * cobraria por isso um `NavHost` dentro do outro.
 *
 * Este arquivo é só o mapa. A ligação de cada destino com o seu ViewModel mora no pacote da tela,
 * pela mesma razão do `:feature-auth`: é lá que ela muda quando a tela muda.
 *
 * Os repositórios chegam por parâmetro para o módulo não depender de `:app` — a seta de dependência
 * aponta para um lado só.
 *
 * @param onSignedOut para onde ir quando a sessão terminar. Quem sabe é `:app`, dono do grafo raiz.
 * @param onSwitchRole `null` quando a conta não tem o papel de treinador. É o que faz o alternador
 *   sumir do menu em vez de aparecer inerte.
 */
fun NavGraphBuilder.studentGraph(
    navController: NavHostController,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
) {
    val dependencies = StudentDependencies(
        authRepository = authRepository,
        userRepository = userRepository,
    )

    navigation(startDestination = StudentRoutes.HOME, route = StudentRoutes.GRAPH) {
        composable(StudentRoutes.HOME) {
            StudentHomeDestination(navController = navController, dependencies = dependencies)
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
            )
        }
    }
}
