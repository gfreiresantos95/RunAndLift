package com.gabrielfreire.runandlift.feature.auth.credentials

import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.validation.EmailError
import com.gabrielfreire.runandlift.feature.auth.validation.PasswordError

/**
 * Estado do formulário de e-mail e senha, compartilhado por entrar e criar conta.
 *
 * Sem tipo de UI e sem recurso de string: os erros são **motivos**, não frases, e é isso que
 * permite testar o ViewModel sem Android. Quem traduz motivo em texto é a tela.
 */
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
