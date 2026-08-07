package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.runtime.Immutable

/**
 * Eventos das telas de entrada, um contrato por fluxo.
 *
 * São dois e não um só porque os fluxos divergem: entrar tem recuperação de senha e leva ao
 * cadastro; cadastrar não tem senha a recuperar e leva à entrada. Um contrato comum obrigaria
 * cada tela a receber campos que ela não usa, e o compilador deixaria de acusar quem esquecesse
 * de ligar um deles.
 */
@Immutable
internal data class SignInActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onForgotPassword: () -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onCreateAccount: () -> Unit,
    val onBack: () -> Unit,
    val onAuthenticated: () -> Unit,
)

/** Eventos da tela de criar conta. Sem "esqueci minha senha": não há senha a esquecer ainda. */
@Immutable
internal data class SignUpActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onGoogleSignIn: () -> Unit,
    val onSignIn: () -> Unit,
    val onBack: () -> Unit,
    val onAuthenticated: () -> Unit,
)
