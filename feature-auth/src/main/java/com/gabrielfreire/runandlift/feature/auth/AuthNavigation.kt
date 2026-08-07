package com.gabrielfreire.runandlift.feature.auth

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
import com.gabrielfreire.runandlift.feature.auth.recovery.PasswordRecoveryScreen
import com.gabrielfreire.runandlift.feature.auth.recovery.PasswordRecoveryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Grafo de entrada: login, cadastro, recuperação e escolha de papel (E1-01, E1-10, E1-02).
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
    val googleSignIn = GoogleSignInRequester(webClientId)

    navigation(startDestination = AuthRoutes.SIGN_IN, route = AuthRoutes.GRAPH) {
        composable(AuthRoutes.SIGN_IN) {
            SignInDestination(navController, authRepository, googleSignIn)
        }
        composable(AuthRoutes.SIGN_UP) {
            SignUpDestination(navController, authRepository, googleSignIn)
        }
        composable(AuthRoutes.RECOVERY) {
            RecoveryDestination(navController, authRepository)
        }
        composable(AuthRoutes.ROLE_SELECTION) {
            RoleSelectionDestination(authRepository, userRepository, onAuthenticatedWithRole)
        }
    }
}

@Composable
private fun SignInDestination(
    navController: NavHostController,
    authRepository: AuthRepository,
    googleSignIn: GoogleSignInRequester,
    viewModel: SignInViewModel = viewModel(
        factory = viewModelFactory { initializer { SignInViewModel(authRepository) } },
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
            onAlternative = { navController.navigate(AuthRoutes.SIGN_UP) },
            onForgotPassword = { navController.navigate(AuthRoutes.RECOVERY) },
            onAuthenticated = { navController.navigate(AuthRoutes.ROLE_SELECTION) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, googleSignIn, viewModel) },
        ),
    )
}

@Composable
private fun SignUpDestination(
    navController: NavHostController,
    authRepository: AuthRepository,
    googleSignIn: GoogleSignInRequester,
    viewModel: SignUpViewModel = viewModel(
        factory = viewModelFactory { initializer { SignUpViewModel(authRepository) } },
    ),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CredentialsScreen(
        state = state,
        labels = CredentialsLabels(
            title = stringResource(R.string.auth_sign_up_title),
            submit = stringResource(R.string.auth_sign_up_action),
            alternative = stringResource(R.string.auth_go_to_sign_in),
        ),
        actions = CredentialsActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            onAlternative = { navController.popBackStack() },
            onAuthenticated = { navController.navigate(AuthRoutes.ROLE_SELECTION) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, googleSignIn, viewModel) },
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
    authRepository: AuthRepository,
    userRepository: UserRepository,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: RoleSelectionViewModel = viewModel(
        factory = viewModelFactory {
            initializer { RoleSelectionViewModel(authRepository, userRepository) }
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
