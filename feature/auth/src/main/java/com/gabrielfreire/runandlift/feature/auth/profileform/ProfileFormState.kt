package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.validation.BirthDateError
import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import com.gabrielfreire.runandlift.feature.auth.validation.NameError
import com.gabrielfreire.runandlift.feature.auth.validation.PhoneError

/**
 * Campos do cadastro que não são credencial.
 *
 * Vive à parte de [CredentialsUiState], e não dentro dele, porque a entrada não tem nenhum deles:
 * juntar os dois faria a tela de entrar carregar um estado de nome, nascimento e aceite que ela
 * nunca preenche nem exibe.
 *
 * [birthDate] e [phone] guardam **só dígitos** — a máscara é apresentação, e o estado que a
 * carregasse obrigaria validação e gravação a limpá-la de novo, cada uma do seu jeito.
 *
 * O estado é **um só para os dois perfis**, e não um por papel. O que muda entre aluno e treinador
 * é quais campos a tela mostra e quais são exigidos, não a natureza do que se coleta; dois estados
 * paralelos duplicariam nome, nascimento e aceite para descrever essa diferença.
 *
 * A régua que o confere é [validated], no arquivo vizinho; a conversão para o que se grava é
 * `SignUpFormDetails`.
 *
 * @param cref registro profissional. Preenchido apenas no cadastro de treinador — o campo nem
 *   aparece para o aluno, e a validação o ignora.
 */
internal data class ProfileFormState(
    val name: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val cref: String = "",
    val acceptedTerms: Boolean = false,
    val marketingOptIn: Boolean = false,
    val nameError: NameError? = null,
    val birthDateError: BirthDateError? = null,
    val phoneError: PhoneError? = null,
    val crefError: CrefError? = null,
    /** Aceite obrigatório em falta. Booleano e não enum: só existe um motivo. */
    val termsMissing: Boolean = false,
) {
    /** Nada pendente. Consultado depois de [validated], nunca antes — antes, tudo parece válido. */
    val isValid: Boolean
        get() = nameError == null &&
            birthDateError == null &&
            phoneError == null &&
            crefError == null &&
            !termsMissing
}

/**
 * O mesmo formulário conferido com a régua do perfil escolhido.
 *
 * Devolve uma cópia com **todos** os erros preenchidos de uma vez, em vez de parar no primeiro:
 * formulário que revela um problema por envio faz a pessoa tentar quatro vezes para descobrir
 * quatro coisas.
 *
 * A diferença entre os dois perfis mora inteira aqui — celular obrigatório e CREF exigido para o
 * treinador, os dois dispensados para o aluno. Espalhar essa decisão entre a tela e o ViewModel é
 * como uma delas acabaria exigindo o que a outra esconde.
 *
 * @param isTrainer perfil escolhido nas boas-vindas. Quando o papel é desconhecido — cadastro
 *   alcançado sem passar por elas — vale a régua do aluno, que é a menos exigente: barrar alguém
 *   por um campo que a tela nem mostrou seria um beco sem saída.
 * @param askName `false` na tela que completa um cadastro vindo do Google, onde o nome já veio do
 *   provedor e o campo não é exibido. Cobrar um campo que não está na tela é um erro sem conserto.
 * @param askConsent `false` quando o aceite desta versão dos termos já está registrado. Repetir o
 *   pedido de consentimento a quem já consentiu não o torna mais válido.
 */
internal fun ProfileFormState.validated(
    isTrainer: Boolean,
    askName: Boolean = true,
    askConsent: Boolean = true,
): ProfileFormState = copy(
    nameError = if (askName) AuthFormValidation.validateName(name) else null,
    birthDateError = AuthFormValidation.validateBirthDate(birthDate),
    phoneError = AuthFormValidation.validatePhone(phone, required = isTrainer),
    crefError = if (isTrainer) AuthFormValidation.validateCref(cref) else null,
    termsMissing = askConsent && !acceptedTerms,
)
