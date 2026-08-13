package com.gabrielfreire.runandlift.feature.auth.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.component.rememberLegalDocumentOpener
import com.gabrielfreire.runandlift.feature.auth.google.requestGoogleSignIn
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthDependencies
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.feature.auth.navigation.continueAfterAuth

/**
 * Liga a tela de entrar ao seu ViewModel e ao grafo.
 *
 * Fica no pacote da tela, e não no do grafo, porque é aqui que ela muda: acrescentar uma ação a
 * [SignInActions] quebra este arquivo, e não o mapa de rotas.
 */
@Composable
internal fun SignInDestination(
    navController: NavHostController,
    dependencies: AuthDependencies,
    role: ActiveRole?,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
    viewModel: SignInViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SignInViewModel(dependencies.authRepository, dependencies.userRepository, role)
            }
        },
    ),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openLegalDocument = rememberLegalDocumentOpener()

    SignInScreen(
        state = state,
        role = role,
        actions = SignInActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSubmit = viewModel::onSubmit,
            // **A única entrada do fluxo de criação de conta.** O perfil escolhido na abertura vai
            // junto na rota: sem ele, o cadastro não teria papel para gravar e jogaria a pessoa na
            // tela de escolha depois de autenticar — a pergunta repetida que o ADR-0010 eliminou.
            //
            // `launchSingleTop` porque cadastro e entrada alternam pelo rodapé: sem isso, ir e
            // voltar empilharia uma tela nova a cada toque.
            onCreateAccount = {
                navController.navigate(AuthRoutes.signUp(role)) { launchSingleTop = true }
            },
            onForgotPassword = { navController.navigate(AuthRoutes.RECOVERY) },
            onAuthenticated = { navController.continueAfterAuth(state, onAuthenticatedWithRole) },
            onGoogleSignIn = { requestGoogleSignIn(scope, context, dependencies.googleSignIn, viewModel) },
            onBack = { navController.popBackStack() },
            onOpenLegalDocument = openLegalDocument,
        ),
    )
}
