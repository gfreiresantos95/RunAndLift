package com.gabrielfreire.runandlift.feature.auth.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Desfecho da obtenção do token do Google. */
internal sealed interface GoogleSignInResult {

    /** Token de ID obtido. Ainda **não** autentica no Firebase — isso é o repositório que faz. */
    data class Token(val idToken: String) : GoogleSignInResult

    /** O usuário fechou a folha de seleção. Não é erro, e não deve virar mensagem vermelha. */
    data object Cancelled : GoogleSignInResult

    /** [cause] guarda a exceção original para a telemetria de E0-11; a tela não deve usá-la. */
    data class Failed(val reason: AuthFailure, val cause: Throwable? = null) : GoogleSignInResult
}

/**
 * Obtém o token de ID do Google pelo Credential Manager (backlog E1-01).
 *
 * Duas coisas que valem saber:
 *
 * - **Credential Manager, não `GoogleSignInClient`.** A API antiga está obsoleta, e a nova unifica
 *   senha, passkey e Google numa folha só.
 * - **Esta classe não autentica.** Ela devolve o token, e quem troca token por sessão é o
 *   `AuthRepository`. Sem essa separação, `:feature-auth` conheceria o Firebase, e a decisão de
 *   provedor de autenticação vazaria para a camada de tela.
 *
 * @param webClientId o cliente OAuth **do tipo web**, não o do Android. É contraintuitivo, mas é o
 *   que o Google exige para emitir um token verificável pelo servidor. Em `:app` ele vem de
 *   `R.string.default_web_client_id`, gerado pelo plugin google-services a partir do
 *   `google-services.json`.
 */
internal class GoogleSignInRequester(private val webClientId: String) {

    /**
     * Abre a folha de seleção de conta. Precisa de um [Context] de Activity — a chamada mostra UI
     * do sistema, então não pode partir de um ViewModel.
     */
    suspend fun requestIdToken(context: Context): GoogleSignInResult {
        val option = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        return try {
            val response = CredentialManager.create(context).getCredential(context, request)
            extractIdToken(response.credential)
        } catch (failure: GetCredentialException) {
            // Um `catch` só, ramificando por tipo: mantém a exceção original disponível em todos
            // os caminhos, inclusive para a telemetria.
            when (failure) {
                // O usuário fechou a folha. Silêncio é a resposta correta.
                is GetCredentialCancellationException -> GoogleSignInResult.Cancelled

                // Nenhuma conta Google utilizável no aparelho.
                is NoCredentialException ->
                    GoogleSignInResult.Failed(AuthFailure.NO_GOOGLE_ACCOUNT, failure)

                else -> GoogleSignInResult.Failed(AuthFailure.UNKNOWN, failure)
            }
        }
    }

    /**
     * A credencial volta como [CustomCredential] e precisa ser convertida — não é uma
     * `GoogleIdTokenCredential` diretamente, apesar de a assinatura sugerir.
     */
    private fun extractIdToken(credential: androidx.credentials.Credential): GoogleSignInResult {
        val isGoogleToken = credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

        if (!isGoogleToken) return GoogleSignInResult.Failed(AuthFailure.UNKNOWN)

        return try {
            val token = GoogleIdTokenCredential.createFrom((credential as CustomCredential).data)
            GoogleSignInResult.Token(token.idToken)
        } catch (parsing: GoogleIdTokenParsingException) {
            GoogleSignInResult.Failed(AuthFailure.UNKNOWN, parsing)
        }
    }
}
