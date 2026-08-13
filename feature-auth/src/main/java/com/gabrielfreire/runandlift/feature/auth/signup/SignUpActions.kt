package com.gabrielfreire.runandlift.feature.auth.signup

import androidx.compose.runtime.Immutable

/**
 * Eventos de credencial da tela de criar conta.
 *
 * Sem "esqueci minha senha": não há senha a esquecer ainda. Sem entrada por Google: a folha do
 * Google não coleta aceite de termos nem data de nascimento, e uma conta que nasce sem esses dois
 * cai na tela seguinte pedindo tudo de novo. Quem prefere Google entra pela tela de entrar, onde
 * a folha é o caminho principal.
 */
@Immutable
internal data class SignUpActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onSignIn: () -> Unit,
    val onBack: () -> Unit,
    val onAuthenticated: () -> Unit,
)
