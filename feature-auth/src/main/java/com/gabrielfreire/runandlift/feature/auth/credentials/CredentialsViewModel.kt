package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.AuthResult
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserAccount
import com.gabrielfreire.runandlift.feature.auth.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.EmailError
import com.gabrielfreire.runandlift.feature.auth.PasswordError
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado do formulário de e-mail e senha. Sem tipo de UI e sem recurso de string. */
internal data class CredentialsUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val failure: AuthFailure? = null,
    val submitting: Boolean = false,
    val authenticated: Boolean = false,
    /**
     * Papel com que a conta segue: gravado agora, no cadastro, ou lido do perfil, ao entrar.
     * Quando é `null`, o papel ainda é desconhecido e a navegação passa pela tela de escolha.
     */
    val resolvedRole: ActiveRole? = null,
    /**
     * A conta existe mas o cadastro está pela metade — o caso de quem entrou pelo Google, que não
     * fornece nascimento, registro profissional nem aceite dos termos. A navegação desvia para a
     * tela de conclusão antes de abrir o app.
     */
    val profileIncomplete: Boolean = false,
)

/**
 * Base de entrar e criar conta: o formulário é o mesmo, muda o que o botão faz.
 *
 * O erro de campo só aparece **depois** da primeira tentativa de envio, e some assim que o usuário
 * volta a digitar. Validar a cada tecla acusa "e-mail inválido" enquanto a pessoa ainda está no
 * meio do endereço, o que atrapalha em vez de ajudar.
 *
 * @param requireStrongPassword `true` no cadastro, onde o comprimento mínimo vale; `false` ao
 *   entrar, onde recusar por tamanho apenas revelaria a regra a quem já tem senha antiga.
 */
internal abstract class CredentialsViewModel(
    private val requireStrongPassword: Boolean,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CredentialsUiState())
    val uiState: StateFlow<CredentialsUiState> = _uiState.asStateFlow()

    protected abstract suspend fun authenticate(email: String, password: String): AuthResult

    /**
     * Com que papel esta conta segue, decidido logo depois de ela existir. Cadastro **grava** o
     * papel escolhido nas boas-vindas; entrada **lê** o papel que a conta já tem.
     *
     * `null` significa "ainda não se sabe", e leva à tela de escolha de papel — é o desfecho de
     * conta antiga sem papel, e também o de uma gravação que falhou.
     *
     * Vale para os dois caminhos de autenticação, e-mail e Google, porque nos dois a conta só
     * ganha `uid` aqui.
     */
    protected open suspend fun resolveRole(account: UserAccount?): ActiveRole? = null

    /**
     * Se ainda falta alguma coisa para a conta poder ser usada neste papel.
     *
     * Falso por padrão porque o cadastro por formulário coleta tudo o que precisa antes de criar a
     * conta — a pergunta só faz sentido em quem entrou por um provedor que não pergunta nada.
     */
    protected open suspend fun profileIncomplete(account: UserAccount?, role: ActiveRole?): Boolean = false

    /**
     * Validação dos campos que esta base não conhece — nome, data de nascimento e aceite, no
     * cadastro. Devolve `false` para interromper o envio, e é responsável por publicar os próprios
     * erros no estado.
     *
     * É chamada **sempre**, mesmo com e-mail ou senha já inválidos, para a tela apontar todos os
     * problemas de uma vez: formulário que revela um erro por envio faz a pessoa tentar três vezes
     * para descobrir três coisas.
     */
    protected open fun validateExtras(): Boolean = true

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, failure = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, failure = null) }
    }

    /** A folha do Google abriu. Bloqueia o formulário enquanto ela estiver na frente. */
    fun onGoogleSignInStarted() {
        _uiState.update { it.copy(submitting = true, failure = null) }
    }

    /**
     * Desfecho da folha do Google.
     *
     * Cancelamento **não** é erro: o usuário fechou a folha de propósito, e pintar a tela de
     * vermelho por isso trata uma decisão dele como falha do app.
     */
    fun onGoogleSignInResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Cancelled ->
                _uiState.update { it.copy(submitting = false) }

            is GoogleSignInResult.Failed ->
                _uiState.update { it.copy(submitting = false, failure = result.reason) }

            is GoogleSignInResult.Token ->
                viewModelScope.launch { complete(authRepository.signInWithGoogle(result.idToken)) }
        }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.submitting) return

        val emailError = AuthFormValidation.validateEmail(current.email)
        val passwordError = AuthFormValidation.validatePassword(
            password = current.password,
            requireMinLength = requireStrongPassword,
        )

        // Avaliada antes do `if` para não ser curto-circuitada: os erros dela precisam aparecer
        // junto com os de e-mail e senha, não no envio seguinte.
        val extrasValid = validateExtras()

        if (emailError != null || passwordError != null || !extrasValid) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(submitting = true, failure = null) }

        // Aparado só aqui, e não a cada tecla: apagar espaço enquanto a pessoa digita faz o cursor
        // parecer travado. O que vai à rede, porém, nunca pode levar o espaço que o teclado do
        // Android sugere depois do domínio — ele vira "e-mail ou senha incorretos" sem explicação.
        viewModelScope.launch { complete(authenticate(current.email.trim(), current.password)) }
    }

    /** Desfecho comum aos dois caminhos de autenticação. */
    private suspend fun complete(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                val role = resolveRole(result.account)
                val incomplete = profileIncomplete(result.account, role)
                _uiState.update {
                    it.copy(
                        submitting = false,
                        authenticated = true,
                        resolvedRole = role,
                        profileIncomplete = incomplete,
                    )
                }
            }

            is AuthResult.Failure ->
                _uiState.update { it.copy(submitting = false, failure = result.reason) }
        }
    }
}
