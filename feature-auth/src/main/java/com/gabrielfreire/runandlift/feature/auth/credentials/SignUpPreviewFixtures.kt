package com.gabrielfreire.runandlift.feature.auth.credentials

// Cenários prontos para os previews do cadastro.
//
// Ficam num arquivo próprio porque os previews que precisam deles passaram de três para nove,
// espalhados por sete arquivos — treze lambdas vazias repetidas em cada um é o tipo de ruído que
// faz a pessoa parar de escrever preview. `internal` e não `private` exatamente por isso.
//
// Os valores aqui obedecem às mesmas regras do estado real: campo mascarado guarda só o conteúdo,
// sem separador, como em produção. Um fixture que guardasse `012345-G/SP` desenharia no preview
// algo que o app nunca produz, e o preview deixaria de ser prova de alguma coisa.
//
// Os textos visíveis não moram aqui: preview de `:feature-auth` usa `stringResource`, porque este
// módulo TEM `strings.xml`. Repetir a frase do app numa constante criaria uma segunda cópia que
// envelhece sozinha.

/** Registro completo e válido, no formato que o campo mascarado guarda. */
internal const val PREVIEW_CREF_CONTENT = "012345GSP"

/** Registro pela metade — só os seis dígitos, sem categoria nem estado. Exercita o erro. */
internal const val PREVIEW_CREF_INCOMPLETE = "012345"

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

internal fun previewSignInActions() = SignInActions(
    onEmailChange = {},
    onPasswordChange = {},
    onSubmit = {},
    onForgotPassword = {},
    onGoogleSignIn = {},
    onCreateAccount = {},
    onBack = {},
    onAuthenticated = {},
    onOpenLegalDocument = {},
)

/** Credenciais preenchidas e sem erro — o estado base de quase todo preview de tela. */
internal fun previewCredentialsState() = CredentialsUiState(
    email = "ana@exemplo.com",
    password = "senha1234",
)

/** Cadastro de treinador preenchido e válido — o cenário base dos previews do perfil. */
internal fun previewTrainerForm() = SignUpFormState(
    name = "Bruno Lima",
    birthDate = "14031988",
    phone = "11912345678",
    cref = PREVIEW_CREF_CONTENT,
    acceptedTerms = true,
)

/** Cadastro de aluno preenchido e válido. Sem CREF: o campo não existe neste perfil. */
internal fun previewStudentForm() = SignUpFormState(
    name = "Ana Ribeiro",
    birthDate = "21051990",
    phone = "11987654321",
    acceptedTerms = true,
)
