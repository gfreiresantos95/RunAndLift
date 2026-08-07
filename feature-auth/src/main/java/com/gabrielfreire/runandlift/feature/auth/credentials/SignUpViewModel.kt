package com.gabrielfreire.runandlift.feature.auth.credentials

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * Criar conta, já com o papel escolhido nas boas-vindas.
 *
 * Gravar o papel aqui é o que faz a escolha de abertura valer alguma coisa: sem isso o app
 * perguntaria "aluno ou treinador?" duas vezes, antes e depois do cadastro, para responder a mesma
 * coisa.
 *
 * Duas decisões embutidas:
 * - **A gravação falhar não invalida o cadastro.** A conta já existe neste ponto; devolver falha
 *   faria a pessoa tentar de novo e receber "e-mail já em uso". Então [resolveRole] devolve
 *   `null`, e a navegação cai na tela de escolha de papel, que tenta de novo com um botão.
 * - **Com Google, a conta pode já existir** — a folha do Google entra e cadastra pela mesma porta.
 *   Nesse caso `addRole` **soma** o papel ao que a conta já tinha, que é o comportamento correto:
 *   quem entrou pelo "criar conta como treinador" está dizendo que quer usar o app como treinador.
 *
 * @param intendedRole papel escolhido antes do login, ou `null` quando o cadastro foi alcançado
 *   pela tela de entrar, sem passar pelas boas-vindas.
 */
internal class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val intendedRole: ActiveRole? = null,
) : CredentialsViewModel(requireStrongPassword = true, authRepository = authRepository) {

    override suspend fun authenticate(email: String, password: String): AuthResult =
        authRepository.signUpWithEmail(email = email, password = password)

    override suspend fun resolveRole(account: UserAccount?): ActiveRole? {
        // Cópia local: o compilador não faz smart cast de propriedade da classe dentro do lambda.
        val role = intendedRole
        if (role == null || account == null) return null

        return runCatching {
            userRepository.addRole(
                uid = account.uid,
                role = role,
                displayName = account.email?.substringBefore('@'),
            )
        }.map { it.activeRole }.getOrNull()
    }
}
