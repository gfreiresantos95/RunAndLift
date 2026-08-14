package com.gabrielfreire.runandlift.feature.auth.signin

import androidx.compose.runtime.Immutable
import com.gabrielfreire.runandlift.feature.auth.component.LegalDocument

/**
 * Eventos da tela de entrar.
 *
 * Existe separado de [SignUpActions] porque os fluxos divergem: entrar tem recuperação de senha,
 * entrada por Google e é a **única porta** para o cadastro; cadastrar não tem senha a recuperar e
 * só volta. Um contrato comum obrigaria cada tela a receber campos que ela não usa, e o compilador
 * deixaria de acusar quem esquecesse de ligar um deles.
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
    /** A folha do Google também **cria** conta, então esta tela também precisa exibir os termos. */
    val onOpenLegalDocument: (LegalDocument) -> Unit,
)
