package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.completeprofile.CompleteProfileDestination
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInRequester
import com.gabrielfreire.runandlift.feature.auth.location.LocationPickerDestination
import com.gabrielfreire.runandlift.feature.auth.onboarding.RoleSelectionDestination
import com.gabrielfreire.runandlift.feature.auth.onboarding.WelcomeDestination
import com.gabrielfreire.runandlift.feature.auth.recovery.PasswordRecoveryDestination
import com.gabrielfreire.runandlift.feature.auth.signin.SignInDestination
import com.gabrielfreire.runandlift.feature.auth.signup.SignUpDestination

/**
 * Grafo de entrada: boas-vindas, login, cadastro, recuperação e escolha de papel (E1-01, E1-10,
 * E1-02).
 *
 * Começa nas boas-vindas, e não no login, porque o perfil escolhido ali decide o funil de cadastro
 * inteiro. Entrar e criar conta são **destinos separados**, cada um com a sua tela: o que os dois
 * pedem já não é o mesmo, e o que prometem nunca foi.
 *
 * O caminho é **linear**: boas-vindas escolhem o perfil, e as duas saídas vão para a entrada;
 * o cadastro só é alcançado pelo rodapé da entrada, e de lá só se volta. Uma porta só para o
 * cadastro é o que garante que o perfil escolhido na abertura chegue inteiro até a gravação.
 *
 * Este arquivo declara **apenas o mapa**: qual rota leva a qual destino. A ligação de cada destino
 * — ViewModel, estado e ações — mora no pacote da tela correspondente, porque é lá que ela muda
 * quando a tela muda.
 *
 * Os repositórios chegam por parâmetro, e não de um container global, para `:feature-auth` não
 * depender de `:app` — o que inverteria a direção dos módulos. Quem injeta é quem tem o grafo de
 * dependências, e isso é `:app`. Chegam **reunidos** em [AuthRepositories] pela razão que o grafo do
 * aluno já tinha descoberto: soltos, esta assinatura ganha um parâmetro a cada tela nova que precise
 * de mais um, e a de quem chama muda junto.
 *
 * @param onAuthenticatedWithRole chamado quando há conta **e** papel definido. Para onde ir quem
 *   decide é `:app`, que conhece os grafos de treinador e de aluno.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    repositories: AuthRepositories,
    webClientId: String,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
) {
    val dependencies = AuthDependencies(
        authRepository = repositories.authRepository,
        userRepository = repositories.userRepository,
        googleSignIn = GoogleSignInRequester(webClientId),
        locationRepository = repositories.locationRepository,
    )

    navigation(startDestination = AuthRoutes.WELCOME, route = AuthRoutes.GRAPH) {
        composable(AuthRoutes.WELCOME) {
            WelcomeDestination(navController)
        }
        composable(route = AuthRoutes.SIGN_IN_PATTERN, arguments = roleArgument()) { entry ->
            SignInDestination(
                navController = navController,
                dependencies = dependencies,
                role = entry.role(),
                onAuthenticatedWithRole = onAuthenticatedWithRole,
            )
        }
        composable(route = AuthRoutes.SIGN_UP_PATTERN, arguments = roleArgument()) { entry ->
            SignUpDestination(
                navController = navController,
                entry = entry,
                dependencies = dependencies,
                intendedRole = entry.role(),
                onAuthenticatedWithRole = onAuthenticatedWithRole,
            )
        }
        composable(route = AuthRoutes.COMPLETE_PROFILE_PATTERN, arguments = roleArgument()) { entry ->
            // Sem papel não há o que completar: o que ainda vai ser perguntado depende dele. Quem
            // chega aqui sem papel passou antes pela escolha, que é quem sabe respondê-lo.
            entry.role()?.let { role ->
                CompleteProfileDestination(
                    navController = navController,
                    entry = entry,
                    dependencies = dependencies,
                    role = role,
                    onAuthenticatedWithRole = onAuthenticatedWithRole,
                )
            }
        }
        composable(AuthRoutes.RECOVERY) {
            PasswordRecoveryDestination(navController, dependencies.authRepository)
        }
        composable(AuthRoutes.ROLE_SELECTION) {
            RoleSelectionDestination(dependencies, onAuthenticatedWithRole)
        }

        // As duas listas de localidade. Ficam **dentro** do grafo de entrada, e não num grafo à
        // parte, porque o resultado volta pela entrada anterior da pilha: quem as abre é o
        // formulário deste grafo, e é para ele que a escolha tem de retornar.
        composable(AuthRoutes.STATE_PICKER) {
            LocationPickerDestination(navController = navController, dependencies = dependencies, uf = null)
        }
        composable(route = AuthRoutes.CITY_PICKER_PATTERN, arguments = ufArgument()) { entry ->
            LocationPickerDestination(
                navController = navController,
                dependencies = dependencies,
                uf = entry.arguments?.getString(AuthRoutes.UF_ARG),
            )
        }
    }
}
