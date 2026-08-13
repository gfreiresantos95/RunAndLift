package com.gabrielfreire.runandlift.feature.auth.signup

/**
 * Ações vazias do cadastro, para os previews da tela e do formulário.
 *
 * Só as de credencial: os campos de perfil têm as suas em `profileform`, porque a conclusão de
 * cadastro usa as mesmas e não usa nenhuma destas.
 */
internal fun previewSignUpActions() = SignUpActions(
    onEmailChange = {},
    onPasswordChange = {},
    onSubmit = {},
    onSignIn = {},
    onBack = {},
    onAuthenticated = {},
)
