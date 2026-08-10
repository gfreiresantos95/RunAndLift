package com.gabrielfreire.runandlift.feature.auth.credentials

/**
 * Ações vazias para os previews do cadastro.
 *
 * Ficam num arquivo próprio porque três previews em dois arquivos precisam delas, e treze lambdas
 * repetidas em cada um é o tipo de ruído que faz a pessoa parar de escrever preview. `internal` e
 * não `private`: o preview do formulário e o da tela inteira montam o mesmo cenário.
 */
internal fun previewSignUpActions() = SignUpActions(
    onEmailChange = {},
    onPasswordChange = {},
    onSubmit = {},
    onSignIn = {},
    onBack = {},
    onAuthenticated = {},
)

internal fun previewSignUpFormActions() = SignUpFormActions(
    onNameChange = {},
    onBirthDateChange = {},
    onPhoneChange = {},
    onCrefChange = {},
    onTermsChange = {},
    onMarketingChange = {},
    onOpenLegalDocument = {},
)

/** Cadastro de treinador preenchido e válido — o cenário base dos previews do perfil. */
internal fun previewTrainerForm() = SignUpFormState(
    name = "Bruno Lima",
    birthDate = "14031988",
    phone = "11912345678",
    cref = "012345-G/SP",
    acceptedTerms = true,
)

/** Cadastro de aluno preenchido e válido. Sem CREF: o campo não existe neste perfil. */
internal fun previewStudentForm() = SignUpFormState(
    name = "Ana Ribeiro",
    birthDate = "21051990",
    phone = "11987654321",
    acceptedTerms = true,
)
