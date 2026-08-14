package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.feature.auth.validation.CityError
import com.gabrielfreire.runandlift.feature.auth.validation.StateError

// Cenários prontos do formulário de perfil, para os previews de cadastro e de conclusão.
//
// Ficam num arquivo próprio porque os previews que precisam deles estão espalhados por três
// pacotes — profileform, signup e completeprofile —, e sete lambdas vazias repetidas em cada um é
// o tipo de ruído que faz a pessoa parar de escrever preview.
//
// Os valores obedecem às mesmas regras do estado real: campo mascarado guarda só o conteúdo, sem
// separador, como em produção. Um fixture que guardasse `012345-G/SP` desenharia no preview algo
// que o app nunca produz, e o preview deixaria de ser prova de alguma coisa.
//
// Os textos visíveis não moram aqui: preview de `:feature-auth` usa `stringResource`, porque este
// módulo TEM `strings.xml`. Repetir a frase do app numa constante criaria uma segunda cópia que
// envelhece sozinha.

/** Registro completo e válido, no formato que o campo mascarado guarda. */
internal const val PREVIEW_CREF_CONTENT = "012345GSP"

/** Registro pela metade — só os seis dígitos, sem categoria nem estado. Exercita o erro. */
internal const val PREVIEW_CREF_INCOMPLETE = "012345"

internal fun previewProfileFormActions() = ProfileFormActions(
    onNameChange = {},
    onBirthDateChange = {},
    onPhoneChange = {},
    onCrefChange = {},
    onOpenStatePicker = {},
    onOpenCityPicker = {},
    onTermsChange = {},
    onMarketingChange = {},
    onOpenLegalDocument = {},
)

/** Cadastro de treinador preenchido e válido — o cenário base dos previews do perfil. */
internal fun previewTrainerForm() = ProfileFormState(
    name = "Bruno Lima",
    birthDate = "14031988",
    phone = "11912345678",
    cref = PREVIEW_CREF_CONTENT,
    stateUf = "MG",
    stateName = "Minas Gerais",
    city = "Belo Horizonte",
    acceptedTerms = true,
)

/** Cadastro de aluno preenchido e válido. Sem CREF: o campo não existe neste perfil. */
internal fun previewStudentForm() = ProfileFormState(
    name = "Ana Ribeiro",
    birthDate = "21051990",
    phone = "11987654321",
    // Cidade de nome composto e estado acentuado: é neles que se vê a largura real da linha, e não
    // no "Acre" que caberia em qualquer lugar.
    stateUf = "SP",
    stateName = "São Paulo",
    city = "São José dos Campos",
    acceptedTerms = true,
)

/** Envio vazio: os dois campos de localidade acusando de uma vez, que é como o formulário responde. */
internal fun previewLocationErrors() = ProfileFormState(
    stateError = StateError.REQUIRED,
    cityError = CityError.REQUIRED,
)
