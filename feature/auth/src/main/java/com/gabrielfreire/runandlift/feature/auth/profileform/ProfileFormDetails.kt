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
 * O nome é **o que foi digitado**, e nada mais. Havia aqui uma queda para o prefixo do e-mail
 * quando o campo vinha vazio, justificada por um caminho que não passa por esta função — o cadastro
 * por formulário exige nome e sobrenome antes de enviar, então o vazio nunca chegava. O que a queda
 * fazia de fato era manter viva a ideia de que `ana@gmail.com` vira "ana": um nome que ninguém
 * escolheu, gravado como se a pessoa o tivesse informado, e exibido assim na lista do treinador.
 *
 * Com a queda, foi embora também o parâmetro `email`, que só existia para alimentá-la — o e-mail
 * que identifica a conta é o da autenticação, e nunca veio deste formulário.
 */
internal fun ProfileFormState.toSignUpDetails(isTrainer: Boolean) = SignUpDetails(
    displayName = name.trim().takeIf { it.isNotEmpty() },
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
 * Diverge da anterior em **um ponto só**: o nome não vem do formulário, porque esta tela não o
 * pergunta — vem do provedor, por [providerName].
 *
 * Esta função já não mandou `displayName` nenhum, no entendimento de que "quem o forneceu foi o
 * provedor e o repositório preserva o que existe". A premissa era falsa: **ninguém gravava**. A
 * folha do Google devolvia o nome, ele parava no SDK, e a conta criada por ali ficava com
 * `users/{uid}.displayName` nulo para sempre — o que só ficou visível quando a home passou a
 * cumprimentar pelo nome e cumprimentou com "Olá!".
 *
 * O repositório escreve o nome **apenas quando ainda não há um**, então reenviá-lo aqui não
 * atropela o nome de quem já tinha.
 *
 * @param providerName nome vindo da conta do provedor, ou `null` quando não há. Em branco conta
 *   como ausente: gravar `""` como nome esconderia a ausência atrás de um valor.
 */
internal fun ProfileFormState.toCompletionDetails(isTrainer: Boolean, providerName: String?) = SignUpDetails(
    displayName = providerName?.trim()?.takeIf { it.isNotEmpty() },
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
