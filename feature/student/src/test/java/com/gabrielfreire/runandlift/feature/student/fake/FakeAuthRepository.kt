package com.gabrielfreire.runandlift.feature.student.fake

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.UserAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [AuthRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Só o que este módulo exercita tem comportamento: saber quem está logado e encerrar a sessão. Os
 * demais métodos existem para satisfazer a interface e falham se alguém os chamar — uma tela de
 * aluno que tente criar conta é erro de arquitetura, e o teste deve dizê-lo em voz alta em vez de
 * devolver um sucesso silencioso.
 *
 * [signOutCount] é contado porque a regra do menu é sobre **encerrar a sessão antes de navegar**:
 * um fake que só devolvesse `Unit` não distinguiria isso de navegar sem ter saído.
 */
internal class FakeAuthRepository(private val signedIn: UserAccount? = ACCOUNT) : AuthRepository {

    var signOutCount: Int = 0
        private set

    override val currentAccount: Flow<UserAccount?> = flowOf(signedIn)

    override fun currentAccountOrNull(): UserAccount? = signedIn

    override suspend fun signOut() {
        signOutCount++
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult = unsupported()

    override suspend fun signInWithEmail(email: String, password: String): AuthResult = unsupported()

    override suspend fun signInWithGoogle(idToken: String): AuthResult = unsupported()

    override suspend fun sendPasswordReset(email: String): AuthResult = unsupported()

    override suspend fun sendEmailVerification(): AuthResult = unsupported()

    override suspend fun reloadAccount(): AuthResult = unsupported()

    private fun unsupported(): Nothing = error("o módulo do aluno não autentica ninguém")

    companion object {
        val ACCOUNT = UserAccount(uid = "u1", email = "ana@exemplo.com", isEmailVerified = true)
    }
}
