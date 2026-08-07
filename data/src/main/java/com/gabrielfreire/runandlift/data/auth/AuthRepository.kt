package com.gabrielfreire.runandlift.data.auth

import com.gabrielfreire.runandlift.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

/**
 * Autenticação (backlog E1-01, E1-10).
 *
 * [currentAccount] não vai à rede: o Firebase Auth mantém a sessão em disco, então quem observa
 * recebe o estado logado já na primeira emissão, inclusive offline. É o que permite a promessa de
 * abrir o treino sem login novo (E6-01).
 *
 * Nenhum método lança por falha esperada — credencial errada, e-mail em uso e rede ausente são
 * estados normais e viram [AuthResult].
 */
interface AuthRepository {

    /** Sessão atual, `null` quando não há ninguém logado. Emite a cada mudança. */
    val currentAccount: Flow<UserAccount?>

    /** Conta logada agora, sem observar. Usado na decisão de rota inicial. */
    fun currentAccountOrNull(): UserAccount?

    suspend fun signUpWithEmail(email: String, password: String): AuthResult

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    /** Autentica com um token de ID já obtido do Google. */
    suspend fun signInWithGoogle(idToken: String): AuthResult

    /** Dispara o e-mail de recuperação. Não revela se o endereço existe. */
    suspend fun sendPasswordReset(email: String): AuthResult

    /** Dispara o e-mail de verificação para a conta logada. */
    suspend fun sendEmailVerification(): AuthResult

    /** Relê o estado da conta no servidor, para saber se o e-mail já foi verificado. */
    suspend fun reloadAccount(): AuthResult

    suspend fun signOut()
}

/** Desfecho de uma operação de autenticação. */
sealed interface AuthResult {

    data class Success(val account: UserAccount?) : AuthResult

    /**
     * Falha esperada, com causa identificada — a UI escolhe a mensagem a partir de [reason].
     * [cause] guarda a exceção original para a telemetria de E0-11; a tela não deve usá-la.
     */
    data class Failure(val reason: AuthFailure, val cause: Throwable? = null) : AuthResult
}

/**
 * Por que a autenticação falhou.
 *
 * O repositório traduz os códigos do Firebase para este conjunto fechado: assim a UI não precisa
 * conhecer strings de erro do SDK, e trocar de provedor de autenticação não alcança as telas.
 */
enum class AuthFailure {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    NO_NETWORK,
    TOO_MANY_ATTEMPTS,
    NOT_SIGNED_IN,

    /** Nenhuma conta Google utilizável no aparelho. Tem mensagem própria porque tem solução própria. */
    NO_GOOGLE_ACCOUNT,
    UNKNOWN,
}
