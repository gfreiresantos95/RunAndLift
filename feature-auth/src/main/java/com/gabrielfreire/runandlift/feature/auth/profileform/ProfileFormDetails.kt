package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation

// A tradução entre o formulário da tela e o que a camada de dados grava, nos dois sentidos.
//
// As três funções estavam em dois arquivos de ViewModel, privadas, e duas delas se chamavam
// `toDetails` — mesmo nome, mesma classe receptora, regras diferentes. Juntas fica visível o que
// elas têm em comum e, principalmente, onde e por que divergem, que é a única pergunta que alguém
// faz ao mexer aqui.
//
// O que as três respeitam: o estado guarda conteúdo cru (dígitos, `012345GSP`) e a forma canônica
// de gravação (`012345-G/SP`, `LocalDate`) é produzida uma vez, no caminho da escrita. Duas
// grafias do mesmo registro no banco seriam dois registros na hora de conferir.

/**
 * Formulário → perfil, no cadastro por e-mail.
 *
 * O nome cai no prefixo do e-mail quando o formulário não passou por aqui — é o caso da conta
 * criada pela folha do Google, em que a tela de escolha de papel é quem grava.
 */
internal fun ProfileFormState.toSignUpDetails(email: String?, isTrainer: Boolean) = SignUpDetails(
    displayName = name.trim().ifEmpty { email?.substringBefore('@') },
    birthDate = AuthFormValidation.parseBirthDate(birthDate),
    phone = phone.ifEmpty { null },
    // Só existe consentimento se a caixa foi marcada. Registrar um aceite que não aconteceu é
    // pior do que não registrar nada.
    consent = PrivacyConsent(
        termsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
        marketingOptIn = marketingOptIn,
    ).takeIf { acceptedTerms },
    cref = if (isTrainer) AuthFormValidation.formatCref(cref) else null,
)

/**
 * Formulário → perfil, na conclusão de cadastro.
 *
 * Diverge da anterior em **um ponto só**: não manda `displayName`. Quem o forneceu foi o provedor,
 * e o repositório já preserva o nome que existe — reenviá-lo daqui não acrescentaria nada e
 * arriscaria sobrescrever o nome real por um derivado do e-mail.
 */
internal fun ProfileFormState.toCompletionDetails(isTrainer: Boolean) = SignUpDetails(
    birthDate = AuthFormValidation.parseBirthDate(birthDate),
    phone = phone.ifEmpty { null },
    consent = PrivacyConsent(
        termsVersion = PrivacyConsent.CURRENT_TERMS_VERSION,
        marketingOptIn = marketingOptIn,
    ).takeIf { acceptedTerms },
    cref = if (isTrainer) AuthFormValidation.formatCref(cref) else null,
)

/**
 * Perfil → formulário: o que já está gravado volta para o campo, na forma que ele entende.
 *
 * É o sentido inverso das duas acima, e existe pela promessa da conclusão de cadastro — pedir só o
 * que falta. Recomeçar do zero um dado que já existe é pedir de novo o que já foi dado.
 */
internal fun ProfileFormState.prefilledFrom(profile: UserProfile?, registration: String?) = copy(
    birthDate = profile?.birthDate?.let(AuthFormValidation::birthDateDigits).orEmpty(),
    phone = profile?.phone.orEmpty(),
    cref = registration?.let(AuthFormValidation::crefContent).orEmpty(),
)
