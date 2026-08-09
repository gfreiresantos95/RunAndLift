package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.runtime.Immutable

/**
 * Eventos das telas de entrada, um contrato por fluxo.
 *
 * São dois e não um só porque os fluxos divergem: entrar tem recuperação de senha, entrada por
 * Google e é a **única porta** para o cadastro; cadastrar não tem senha a recuperar e só volta.
 * Um contrato comum obrigaria cada tela a receber campos que ela não usa, e o compilador deixaria
 * de acusar quem esquecesse de ligar um deles.
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

/**
 * Eventos dos campos de perfil do cadastro.
 *
 * Separado de [SignUpActions] por duas razões: um contrato só passaria de dez lambdas, que é o
 * tamanho em que ninguém mais confere se todas foram ligadas; e são coisas de natureza diferente —
 * um lado autentica, o outro descreve quem é a pessoa.
 */
@Immutable
internal data class SignUpFormActions(
    val onNameChange: (String) -> Unit,
    val onBirthDateChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onTermsChange: (Boolean) -> Unit,
    val onMarketingChange: (Boolean) -> Unit,
    val onOpenLegalDocument: (LegalDocument) -> Unit,
)

/** Documentos que o cadastro precisa deixar a um toque de distância antes de pedir o aceite. */
internal enum class LegalDocument { TERMS, PRIVACY }
