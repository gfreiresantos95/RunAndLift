package com.gabrielfreire.runandlift.feature.auth.credentials

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.ProfileCompletion

/**
 * Entrar com conta existente.
 *
 * **Não grava papel nenhum**, mesmo que a pessoa tenha passado pelas boas-vindas: quem já tem
 * conta já tem papel em `users/{uid}`, e sobrescrevê-lo com uma escolha de tela de abertura
 * mudaria o app inteiro de alguém que só queria entrar. Aqui o papel é lido, não decidido.
 *
 * Custo declarado: **0 leitura** do Firestore quando `users/{uid}` já está no cache — o caso de
 * quem reinstala é 1 leitura, uma vez. Ler aqui é o que evita perguntar o papel a quem já
 * respondeu.
 *
 * @param intendedRole perfil escolhido nas boas-vindas. Só entra em cena quando a conta **não tem**
 *   papel gravado, que é o caso de quem acabou de nascer pela folha do Google: ali não há o que
 *   ler, e a escolha da abertura é a única resposta que existe. Quem grava, ainda assim, é a tela
 *   de conclusão de cadastro — esta continua sem escrever nada.
 */
internal class SignInViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val intendedRole: ActiveRole? = null,
) : CredentialsViewModel(requireStrongPassword = false, authRepository = authRepository) {

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signInWithEmail(email = email, password = password)

    override suspend fun resolveRole(account: UserAccount?): ActiveRole? {
        val uid = account?.uid ?: return intendedRole

        // Falha de leitura vira `null`, não exceção: sem papel conhecido o fluxo apenas segue para
        // a tela de escolha, que é um desfecho correto — travar a entrada não seria.
        return runCatching { userRepository.profile(uid) }.getOrNull()?.activeRole ?: intendedRole
    }

    /**
     * A folha do Google entra **e cria conta** pela mesma porta, então esta é a tela em que uma
     * conta pode aparecer sem nascimento, sem registro profissional e sem aceite de termos.
     */
    override suspend fun profileIncomplete(account: UserAccount?, role: ActiveRole?): Boolean {
        if (account == null || role == null) return false

        return ProfileCompletion.missing(userRepository, account.uid, role).any
    }
}
