package com.gabrielfreire.runandlift.data.auth

import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

internal class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    override val currentAccount: Flow<UserAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAccount())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.flowOn(dispatchers.io)

    override fun currentAccountOrNull(): UserAccount? = firebaseAuth.currentUser?.toAccount()

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult = authCall {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
        // A verificação vai junto do cadastro: pedir depois, na prática, é nunca pedir (E1-10).
        firebaseAuth.currentUser?.sendEmailVerification()?.await()
        AuthResult.Success(firebaseAuth.currentUser?.toAccount())
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult = authCall {
        val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
        AuthResult.Success(result.user?.toAccount())
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult = authCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        AuthResult.Success(result.user?.toAccount())
    }

    override suspend fun sendPasswordReset(email: String): AuthResult = authCall {
        firebaseAuth.sendPasswordResetEmail(email.trim()).await()
        AuthResult.Success(null)
    }

    override suspend fun sendEmailVerification(): AuthResult = authCall {
        val user = firebaseAuth.currentUser
            ?: return@authCall AuthResult.Failure(AuthFailure.NOT_SIGNED_IN)
        user.sendEmailVerification().await()
        AuthResult.Success(user.toAccount())
    }

    override suspend fun reloadAccount(): AuthResult = authCall {
        val user = firebaseAuth.currentUser
            ?: return@authCall AuthResult.Failure(AuthFailure.NOT_SIGNED_IN)
        user.reload().await()
        AuthResult.Success(firebaseAuth.currentUser?.toAccount())
    }

    override suspend fun signOut() = withContext(dispatchers.io) { firebaseAuth.signOut() }

    /**
     * Roda em I/O e devolve falha em vez de propagar exceção do SDK.
     *
     * Qual motivo cada exceção vira é decisão, e mora em [AuthFailureMapping] — separada porque a
     * **ordem** entre elas é regra (as classes do Firebase herdam umas das outras) e testar ordem
     * dentro de um `try/catch` privado não é possível. A exceção original segue em `cause`, para a
     * telemetria de E0-11.
     *
     * `FirebaseAuthException` inteira, e não as quatro subclasses uma a uma: o que o SDK lançar de
     * novo vira [AuthFailure.UNKNOWN] e uma frase na tela, em vez de subir e derrubar o app. Quem
     * escolhe a frase é a UI, e "não foi possível entrar" é sempre melhor do que fechar sozinho.
     */
    private suspend fun authCall(block: suspend () -> AuthResult): AuthResult = withContext(dispatchers.io) {
        try {
            block()
        } catch (failure: FirebaseAuthException) {
            AuthResult.Failure(AuthFailureMapping.reasonFor(failure), failure)
        } catch (failure: FirebaseTooManyRequestsException) {
            AuthResult.Failure(AuthFailureMapping.reasonFor(failure), failure)
        } catch (failure: FirebaseNetworkException) {
            AuthResult.Failure(AuthFailureMapping.reasonFor(failure), failure)
        } catch (failure: IOException) {
            AuthResult.Failure(AuthFailureMapping.reasonFor(failure), failure)
        }
    }

    private fun FirebaseUser.toAccount() = UserAccount(
        uid = uid,
        email = email,
        isEmailVerified = isEmailVerified,
        // Vem preenchido pelo Google e vazio no cadastro por e-mail. A regra de por que vazio vira
        // ausência está em [ProviderName], que tem teste próprio.
        displayName = ProviderName.of(displayName),
    )
}
