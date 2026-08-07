package com.gabrielfreire.runandlift.feature.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsViewModel
import com.gabrielfreire.runandlift.feature.auth.credentials.SignInActions
import com.gabrielfreire.runandlift.feature.auth.credentials.SignInScreen
import com.gabrielfreire.runandlift.feature.auth.credentials.SignInViewModel
import com.gabrielfreire.runandlift.feature.auth.credentials.SignUpActions
import com.gabrielfreire.runandlift.feature.auth.credentials.SignUpScreen
import com.gabrielfreire.runandlift.feature.auth.credentials.SignUpViewModel
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInRequester
import com.gabrielfreire.runandlift.feature.auth.onboarding.RoleSelectionScreen
import com.gabrielfreire.runandlift.feature.auth.onboarding.RoleSelectionViewModel
import com.gabrielfreire.runandlift.feature.auth.onboarding.WelcomeScreen
import com.gabrielfreire.runandlift.feature.auth.recovery.PasswordRecoveryScreen
import com.gabrielfreire.runandlift.feature.auth.recovery.PasswordRecoveryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Grafo de entrada: boas-vindas, login, cadastro, recuperação e escolha de papel (E1-01, E1-10,
 * E1-02).
 *
 * Começa nas boas-vindas, e não no login, porque o perfil escolhido ali decide o funil de cadastro
 * inteiro. Entrar e criar conta são **destinos separados**, cada um com a sua tela: o que os dois
 * pedem é igual, o que prometem não é.
 *
 * Os repositórios chegam por parâmetro, e não de um container global, para `:feature-auth` não
 * depender de `:app` — o que inverteria a direção dos módulos. Quem injeta é quem tem o grafo de
 * dependências, e isso é `:app`.
 *
 * @param onAuthenticatedWithRole chamado quando há conta **e** papel definido. Para onde ir quem
 *   decide é `:app`, que conhece os grafos de treinador e de aluno.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    authRepository: AuthRepository,
    userRepository: UserRepository,
    webClientId: String,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
) {
    val dependencies = AuthDependencies(
        authRepository = authRepository,
        userRepository = userRepository,
        googleSignIn = GoogleSignInRequester(webClientId),
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
                dependencies = dependencies,
                intendedRole = entry.role(),
                onAuthenticatedWithRole = onAuthenticatedWithRole,
            )
        }
        composable(AuthRoutes.RECOVERY) {
            RecoveryDestination(navController, dependencies.authRepository)
        }
        composable(AuthRoutes.ROLE_SELECTION) {
            RoleSelectionDestination(dependencies, onAuthenticatedWithRole)
        }
    }
}

/**
 * O que os destinos do grafo precisam e não podem buscar sozinhos.
 *
 * Agrupado porque os três atravessam o grafo inteiro juntos: soltos, cada assinatura repetiria a
 * mesma lista, e nenhum deles significa alguma coisa isolado do fluxo de entrada.
 */
@Immutable
internal data class AuthDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val googleSignIn: GoogleSignInRequester,
)

/** Argumento de perfil, opcional e com padrão nulo — as duas telas funcionam sem ele. */
private fun roleArgument() = listOf(
    navArgument(AuthRoutes.ROLE_ARG) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    },
)

private fun NavBackStackEntry.role(): ActiveRole? = ActiveRole.fromStorage(arguments?.getString(AuthRoutes.ROLE_ARG))

/**
 * Boas-vindas. Sem estado a guardar: o toque no papel já é a navegação, e o papel escolhido vira
 * argumento da rota de cadastro — que é onde ele vai ser gravado, depois de a conta existir.
 *
 * Quem já tem conta chega ao login pelo "Já tenho conta" do cadastro. Sai um toque a mais para
 * quem volta, e some a saída lateral que competia com a única decisão desta tela.
 */
@Composable
private fun WelcomeDestination(navController: NavHostController) {
    WelcomeScreen(onSelectRole = { navController.navigate(AuthRoutes.signIn(it)) })
}

@Composable
private fun SignInDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
    role: ActiveRole?,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: SignInViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SignInViewModel(dependencies.authRepository, dependencies.userRepository) }
        },
    ),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignInScreen(
        state = state,
        role = role,
        actions = SignInActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // Desempilha em vez de navegar: o cadastro está logo abaixo, com o perfil que a pessoa
            // escolheu na abertura. Empilhar um segundo cadastro perderia essa escolha.
            onCreateAccount = { navController.navigate(AuthRoutes.signUp(role)) },
            onForgotPassword = { navController.navigate(AuthRoutes.RECOVERY) },
            onAuthenticated = { navController.continueAfterAuth(state.resolvedRole, onAuthenticatedWithRole) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, dependencies.googleSignIn, viewModel) },
            onBack = { navController.popBackStack() },
        ),
    )
}

@Composable
private fun SignUpDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
    intendedRole: ActiveRole?,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: SignUpViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SignUpViewModel(dependencies.authRepository, dependencies.userRepository, intendedRole)
            }
        },
    ),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignUpScreen(
        state = state,
        role = intendedRole,
        actions = SignUpActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // `launchSingleTop` para o par cadastro/entrar alternar sem crescer: daqui empilha, e
            // de lá desempilha. A pilha antes de autenticar nunca passa de três telas. O perfil vai
            // junto, mas só como etiqueta — entrar não grava papel.
            onSignIn = {
                navController.navigate(AuthRoutes.signIn(intendedRole)) { launchSingleTop = true }
            },
            onAuthenticated = { navController.continueAfterAuth(state.resolvedRole, onAuthenticatedWithRole) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, dependencies.googleSignIn, viewModel) },
            onBack = { navController.popBackStack() },
        ),
    )
}

@Composable
private fun RecoveryDestination(
    navController: NavHostController,
    authRepository: AuthRepository,
    viewModel: PasswordRecoveryViewModel = viewModel(
        factory = viewModelFactory { initializer { PasswordRecoveryViewModel(authRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PasswordRecoveryScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onSubmit = viewModel::onSubmit,
        onBack = { navController.popBackStack() },
    )
}

@Composable
private fun RoleSelectionDestination(
    dependencies: AuthDependencies,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: RoleSelectionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RoleSelectionViewModel(dependencies.authRepository, dependencies.userRepository)
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RoleSelectionScreen(
        state = state,
        onSelect = viewModel::onSelect,
        onConfirm = viewModel::onConfirm,
        onConfirmed = onAuthenticatedWithRole,
    )
}

/**
 * Para onde ir depois de autenticar: direto ao grafo do papel quando ele já é conhecido, e para a
 * escolha de papel quando não é.
 *
 * Uma função só, usada por entrar e por cadastrar, porque a regra é a mesma nas duas — e porque
 * duplicá-la é como uma das duas telas acabaria perguntando o papel a quem já tem.
 */
private fun NavHostController.continueAfterAuth(role: ActiveRole?, onAuthenticatedWithRole: (ActiveRole) -> Unit) {
    if (role != null) onAuthenticatedWithRole(role) else navigate(AuthRoutes.ROLE_SELECTION)
}

/**
 * Abre a folha do Google e entrega o desfecho ao ViewModel.
 *
 * Fica na camada de tela, e não no ViewModel, porque a chamada mostra UI do sistema e exige um
 * Context de Activity — ViewModel que segura Context vaza a tela inteira.
 */
private fun requestGoogleSignIn(
    scope: CoroutineScope,
    context: Context,
    googleSignIn: GoogleSignInRequester,
    viewModel: CredentialsViewModel,
) {
    viewModel.onGoogleSignInStarted()
    scope.launch { viewModel.onGoogleSignInResult(googleSignIn.requestIdToken(context)) }
}
