package com.gabrielfreire.runandlift.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gabrielfreire.runandlift.R
import com.gabrielfreire.runandlift.core.designsystem.AppMotion
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.di.AppContainer
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRepositories
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.feature.auth.navigation.authGraph
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.studentGraph
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies
import com.gabrielfreire.runandlift.feature.trainer.navigation.trainerGraph

/**
 * Grafo raiz (backlog E0-08).
 *
 * Três grafos irmãos no mesmo nível: entrada, treinador e aluno. O destino inicial vem decidido de
 * fora, por [startDestination], porque quem sabe se há sessão e qual é o papel ativo é o
 * `MainViewModel` — e ele decide isso **antes** da primeira composição, para a splash não sair
 * mostrando a tela errada por um frame.
 *
 * Cada grafo de papel agora mora no seu módulo. `:app` continua sendo quem os costura e quem sabe
 * para onde ir ao sair da conta ou ao trocar de papel: as features não conhecem uma à outra nem a
 * rota de entrada.
 */
@Composable
fun RunAndLiftNavHost(
    startDestination: String,
    container: AppContainer,
    canSwitchRole: Boolean,
    onSwitchRole: () -> Unit,
    onAuthenticated: (ActiveRole, (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Lido aqui, e não dentro do NavHost: o builder do grafo não é um escopo @Composable.
    //
    // Recurso gerado pelo plugin google-services a partir do google-services.json. É o cliente
    // OAuth do tipo *web*, e não o do Android — é o que o Google exige para emitir um token
    // verificável pelo servidor. Vem de :app porque só aqui o plugin está aplicado.
    val webClientId = stringResource(R.string.default_web_client_id)

    // `null` some com o alternador do menu, em vez de deixá-lo lá sem efeito.
    val switchRole = onSwitchRole.takeIf { canSwitchRole }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Eixo compartilhado horizontal, e não o esmaecimento de 700 ms que vem por padrão: aquele
        // é lento o bastante para parecer travamento e, por não ter direção, faz ir e voltar
        // parecerem o mesmo movimento. Os valores vivem em `AppMotion` — ver o porquê de cada um lá.
        enterTransition = { AppMotion.forwardEnter },
        exitTransition = { AppMotion.forwardExit },
        popEnterTransition = { AppMotion.backEnter },
        popExitTransition = { AppMotion.backExit },
    ) {
        authGraph(
            navController = navController,
            repositories = AuthRepositories(
                authRepository = container.authRepository,
                userRepository = container.userRepository,
                locationRepository = container.locationRepository,
            ),
            webClientId = webClientId,
            // Para onde ir depois de autenticar não é decisão do fluxo de entrada, e desde o
            // onboarding também não é uma constante: um aluno recém-criado vai para o passo a
            // passo, e quem já o respondeu vai para a home. Quem sabe responder isso é o
            // `MainViewModel`, e a resposta chega por callback porque envolve uma leitura.
            onAuthenticatedWithRole = { role ->
                onAuthenticated(role) { destination ->
                    navController.navigateAfterAuth(destination)
                }
            },
        )

        trainerGraph(
            navController = navController,
            dependencies = TrainerDependencies(
                authRepository = container.authRepository,
                userRepository = container.userRepository,
                trainerRepository = container.trainerRepository,
                locationRepository = container.locationRepository,
            ),
            onSignedOut = { navController.navigateToAuth() },
            onSwitchRole = switchRole,
        )

        studentGraph(
            navController = navController,
            dependencies = StudentDependencies(
                authRepository = container.authRepository,
                userRepository = container.userRepository,
                studentRepository = container.studentRepository,
                locationRepository = container.locationRepository,
            ),
            onSignedOut = { navController.navigateToAuth() },
            onSwitchRole = switchRole,
        )
    }
}

/**
 * Leva para onde a autenticação decidiu — a home do papel, ou o onboarding do aluno novo.
 *
 * Remove o fluxo de entrada da pilha: depois de autenticado, "voltar" na primeira tela deve sair do
 * app, e não regressar ao login com sessão ativa.
 */
internal fun NavHostController.navigateAfterAuth(destination: String) {
    navigate(destination) {
        launchSingleTop = true
        popUpTo(AuthRoutes.GRAPH) { inclusive = true }
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

/**
 * Volta ao fluxo de entrada depois de sair da conta.
 *
 * Esvazia a pilha inteira (`popUpTo(graph.id) { inclusive = true }`): sem isso, o botão voltar na
 * tela de boas-vindas devolveria a home do papel — uma tela de quem acabou de sair, montada com o
 * estado que ainda estivesse em memória.
 */
internal fun NavHostController.navigateToAuth() {
    navigate(AuthRoutes.GRAPH) {
        launchSingleTop = true
        popUpTo(graph.id) { inclusive = true }
    }
}
