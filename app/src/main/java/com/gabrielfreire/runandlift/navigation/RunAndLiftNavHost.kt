package com.gabrielfreire.runandlift.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gabrielfreire.runandlift.R
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.AuthRoutes
import com.gabrielfreire.runandlift.feature.auth.authGraph

/**
 * Grafo raiz (backlog E0-08).
 *
 * Três grafos irmãos no mesmo nível: entrada, treinador e aluno. O destino inicial vem decidido de
 * fora, por [startDestination], porque quem sabe se há sessão e qual é o papel ativo é o
 * `MainViewModel` — e ele decide isso **antes** da primeira composição, para a splash não sair
 * mostrando a tela errada por um frame.
 */
@Composable
fun RunAndLiftNavHost(
    startDestination: String,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    canSwitchRole: Boolean,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Lido aqui, e não dentro do NavHost: o builder do grafo não é um escopo @Composable.
    //
    // Recurso gerado pelo plugin google-services a partir do google-services.json. É o cliente
    // OAuth do tipo *web*, e não o do Android — é o que o Google exige para emitir um token
    // verificável pelo servidor. Vem de :app porque só aqui o plugin está aplicado.
    val webClientId = stringResource(R.string.default_web_client_id)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        authGraph(
            navController = navController,
            authRepository = authRepository,
            userRepository = userRepository,
            webClientId = webClientId,
            onAuthenticatedWithRole = { role ->
                navController.navigateToRole(role, clearAuth = true)
            },
        )

        trainerGraph(onSwitchRole = onSwitchRole, canSwitchRole = canSwitchRole)
        studentGraph(onSwitchRole = onSwitchRole, canSwitchRole = canSwitchRole)
    }
}

/**
 * Leva para o grafo do papel.
 *
 * Com [clearAuth], remove o fluxo de entrada da pilha: depois de autenticado, "voltar" na tela
 * inicial deve sair do app, e não regressar ao login com sessão ativa.
 */
internal fun NavHostController.navigateToRole(role: ActiveRole, clearAuth: Boolean) {
    navigate(RoleRoutes.graphFor(role)) {
        launchSingleTop = true
        if (clearAuth) {
            popUpTo(AuthRoutes.GRAPH) { inclusive = true }
        } else {
            // Troca de papel: descarta a pilha do papel anterior, senão "voltar" atravessaria
            // de um papel para o outro.
            popUpTo(graph.id) { inclusive = true }
        }
    }
}
