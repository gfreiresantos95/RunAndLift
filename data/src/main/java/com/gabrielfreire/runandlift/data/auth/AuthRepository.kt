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
