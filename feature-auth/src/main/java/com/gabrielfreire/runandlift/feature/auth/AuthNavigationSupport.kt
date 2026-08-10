package com.gabrielfreire.runandlift.feature.auth

import android.content.Context
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsUiState
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsViewModel
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Peças de apoio do grafo de entrada: o argumento de perfil, a regra de para onde ir depois de
// autenticar e a chamada da folha do Google.
//
// Separadas de AuthNavigation.kt porque são de outra natureza — lá estão os destinos, aqui as
// decisões que atravessam mais de um deles.

/** Argumento de perfil, opcional e com padrão nulo — as telas do fluxo funcionam sem ele. */
internal fun roleArgument() = listOf(
    navArgument(AuthRoutes.ROLE_ARG) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    },
)

internal fun NavBackStackEntry.role(): ActiveRole? = ActiveRole.fromStorage(arguments?.getString(AuthRoutes.ROLE_ARG))

/**
 * Para onde ir depois de autenticar, em três desfechos e nesta ordem:
 *
 * 1. **sem papel** — nem gravado na conta nem escolhido nas boas-vindas: escolha de papel, que é o
 *    único caso em que ela ainda aparece;
 * 2. **papel conhecido e cadastro incompleto** — quem entrou pelo Google: conclusão de cadastro,
 *    levando o papel junto para ser gravado lá;
 * 3. **papel conhecido e cadastro completo**: direto para o grafo do papel.
 *
 * Uma função só, usada por entrar e por cadastrar, porque a regra é a mesma nas duas — e porque
 * duplicá-la é como uma das duas telas acabaria perguntando o papel a quem já tem.
 */
internal fun NavHostController.continueAfterAuth(
    state: CredentialsUiState,
    onAuthenticatedWithRole: (ActiveRole) -> Unit,
) {
    val role = state.resolvedRole

    when {
        role == null -> navigate(AuthRoutes.ROLE_SELECTION)
        state.profileIncomplete -> navigate(AuthRoutes.completeProfile(role))
        else -> onAuthenticatedWithRole(role)
    }
}

/**
 * Abre a folha do Google e entrega o desfecho ao ViewModel.
 *
 * Fica na camada de tela, e não no ViewModel, porque a chamada mostra UI do sistema e exige um
 * Context de Activity — ViewModel que segura Context vaza a tela inteira.
 */
internal fun requestGoogleSignIn(
    scope: CoroutineScope,
    context: Context,
    googleSignIn: GoogleSignInRequester,
    viewModel: CredentialsViewModel,
) {
    viewModel.onGoogleSignInStarted()
    scope.launch { viewModel.onGoogleSignInResult(googleSignIn.requestIdToken(context)) }
}
