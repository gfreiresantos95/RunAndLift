package com.gabrielfreire.runandlift.feature.auth.fake

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.UserAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [AuthRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Conta os envios em [calls] porque várias regras do fluxo são sobre **não chamar a rede**:
 * formulário inválido não autentica, e toque duplo no botão não cria duas contas. Um mock que só
 * devolve valor não afirma nenhuma das duas.
 *
 * @param result desfecho de toda operação. Um só para todas porque nenhum teste precisa que
 *   entrar e recuperar senha falhem de formas diferentes na mesma execução.
 * @param signedIn conta devolvida por [currentAccountOrNull]. `null` simula sessão perdida, que é
 *   o caso que a escolha de papel e a conclusão de cadastro precisam tratar.
 */
internal class FakeAuthRepository(
    private val result: AuthResult = AuthResult.Success(ACCOUNT),
    private val signedIn: UserAccount? = null,
) : AuthRepository {

    var calls: Int = 0
        private set

    override val currentAccount: Flow<UserAccount?> = flowOf(signedIn)

    override fun currentAccountOrNull(): UserAccount? = signedIn

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        calls++
        return result
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        calls++
        return result
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult = result

    override suspend fun sendPasswordReset(email: String): AuthResult {
        calls++
        lastResetEmail = email
        return result
    }

    /** O que de fato foi enviado ao servidor — serve para afirmar que o espaço do teclado foi aparado. */
    var lastResetEmail: String? = null
        private set

    override suspend fun sendEmailVerification(): AuthResult = result

    override suspend fun reloadAccount(): AuthResult = result

    override suspend fun signOut() = Unit

    companion object {
        val ACCOUNT = UserAccount(uid = "u1", email = "a@b.com", isEmailVerified = false)
    }
}
