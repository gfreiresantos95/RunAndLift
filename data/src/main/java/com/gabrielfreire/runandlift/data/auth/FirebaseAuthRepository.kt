package com.gabrielfreire.runandlift.data.auth

import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
     * Roda em I/O e traduz exceção do SDK para [AuthFailure].
     *
     * A tradução mora aqui, e não na tela, porque código de erro do Firebase é detalhe do
     * provedor: a UI decide a mensagem a partir de um conjunto fechado, que não muda se um dia o
     * provedor mudar. A exceção original segue em `cause`, para a telemetria de E0-11.
     */
    private suspend fun authCall(block: suspend () -> AuthResult): AuthResult = withContext(dispatchers.io) {
        try {
            block()
        } catch (failure: FirebaseAuthWeakPasswordException) {
            AuthResult.Failure(AuthFailure.WEAK_PASSWORD, failure)
        } catch (failure: FirebaseAuthUserCollisionException) {
            AuthResult.Failure(AuthFailure.EMAIL_ALREADY_IN_USE, failure)
        } catch (failure: FirebaseAuthInvalidUserException) {
            AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS, failure)
        } catch (failure: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure(AuthFailure.INVALID_CREDENTIALS, failure)
        } catch (failure: FirebaseTooManyRequestsException) {
            AuthResult.Failure(AuthFailure.TOO_MANY_ATTEMPTS, failure)
        } catch (failure: FirebaseNetworkException) {
            AuthResult.Failure(AuthFailure.NO_NETWORK, failure)
        } catch (failure: IOException) {
            AuthResult.Failure(AuthFailure.NO_NETWORK, failure)
        }
    }

    private fun FirebaseUser.toAccount() = UserAccount(
        uid = uid,
        email = email,
        isEmailVerified = isEmailVerified,
        // Vem preenchido pelo Google e vazio no cadastro por e-mail. `takeIf` porque o SDK devolve
        // string vazia, e não nulo, quando o provedor não informou nome — e "" gravado como nome
        // seria pior que nome nenhum: o app não teria como distinguir ausência de escolha.
        displayName = displayName?.takeIf { it.isNotBlank() },
    )
}
