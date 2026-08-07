package com.gabrielfreire.runandlift.feature.auth

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsActions
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsLabels
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsScreen
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsViewModel
import com.gabrielfreire.runandlift.feature.auth.credentials.SignInViewModel
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
 * Começa nas boas-vindas, e não no login, porque o papel escolhido ali decide o funil de cadastro
 * inteiro. Quem já tem conta atravessa a tela com um toque em "Já tenho conta".
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
        composable(AuthRoutes.SIGN_IN) {
            SignInDestination(navController, dependencies, onAuthenticatedWithRole)
        }
        composable(
            route = AuthRoutes.SIGN_UP_PATTERN,
            arguments = listOf(
                navArgument(AuthRoutes.ROLE_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            SignUpDestination(
                navController = navController,
                dependencies = dependencies,
                intendedRole = ActiveRole.fromStorage(entry.arguments?.getString(AuthRoutes.ROLE_ARG)),
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

/**
 * Boas-vindas. Sem estado a guardar: o toque no papel já é a navegação, e o papel escolhido vira
 * argumento da rota de cadastro — que é onde ele vai ser gravado, depois de a conta existir.
 *
 * Quem já tem conta chega ao login pelo "Já tenho conta" do cadastro. Sai um toque a mais para
 * quem volta, e some a saída lateral que competia com a única decisão desta tela.
 */
@Composable
private fun WelcomeDestination(navController: NavHostController) {
    WelcomeScreen(onSelectRole = { navController.navigate(AuthRoutes.signUp(it)) })
}

@Composable
private fun SignInDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
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

    CredentialsScreen(
        state = state,
        labels = CredentialsLabels(
            title = stringResource(R.string.auth_sign_in_title),
            submit = stringResource(R.string.auth_sign_in_action),
            alternative = stringResource(R.string.auth_go_to_sign_up),
        ),
        actions = CredentialsActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // Volta às boas-vindas em vez de abrir o cadastro direto: quem não tem conta precisa
            // escolher o papel antes, e é lá que a escolha acontece.
            onAlternative = { navController.popBackStack(AuthRoutes.WELCOME, inclusive = false) },
            onForgotPassword = { navController.navigate(AuthRoutes.RECOVERY) },
            onAuthenticated = { navController.continueAfterAuth(state.resolvedRole, onAuthenticatedWithRole) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, dependencies.googleSignIn, viewModel) },
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

    CredentialsScreen(
        state = state,
        labels = CredentialsLabels(
            title = stringResource(intendedRole.signUpTitle()),
            submit = stringResource(R.string.auth_sign_up_action),
            alternative = stringResource(R.string.auth_go_to_sign_in),
        ),
        actions = CredentialsActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // Substitui o cadastro na pilha em vez de empilhar: sem isso, "voltar" na tela de
            // entrar levaria de volta a um cadastro que a pessoa acabou de dizer não querer.
            onAlternative = {
                navController.navigate(AuthRoutes.SIGN_IN) {
                    popUpTo(AuthRoutes.WELCOME)
                    launchSingleTop = true
                }
            },
            onAuthenticated = { navController.continueAfterAuth(state.resolvedRole, onAuthenticatedWithRole) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, dependencies.googleSignIn, viewModel) },
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

/** Título do cadastro. Repetir o papel escolhido confirma, sem uma tela a mais, o que vai ser criado. */
@StringRes
private fun ActiveRole?.signUpTitle(): Int = when (this) {
    ActiveRole.STUDENT -> R.string.auth_sign_up_title_student
    ActiveRole.TRAINER -> R.string.auth_sign_up_title_trainer
    null -> R.string.auth_sign_up_title
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
