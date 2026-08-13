package com.gabrielfreire.runandlift.feature.auth.google

import android.content.Context
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Abre a folha do Google e entrega o desfecho ao ViewModel.
 *
 * Fica na camada de tela, e não no ViewModel, porque a chamada mostra UI do sistema e exige um
 * `Context` de Activity — ViewModel que segura `Context` vaza a tela inteira. Fica neste pacote, e
 * não no de navegação, porque o que ela conhece é a folha do Google, não o grafo.
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
