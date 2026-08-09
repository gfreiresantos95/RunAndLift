package com.gabrielfreire.runandlift.feature.auth

import java.time.LocalDate
import java.time.Period

/**
 * Validação de formulário, separada dos ViewModels para ser testável sem Android.
 *
 * Valida apenas o que dá para saber sem ir ao servidor: campo vazio, formato e coerência de data.
 * Se a senha está correta ou o e-mail existe, quem responde é o servidor — validar isso aqui seria
 * adivinhar.
 */
internal object AuthFormValidation {

    /** Piso do próprio Firebase Auth. Repetido aqui para o erro aparecer antes da ida à rede. */
    const val MIN_PASSWORD_LENGTH = 6

    /**
     * Idade mínima para criar conta sozinho.
     *
     * Não é número de conveniência: a LGPD trata dado de criança e adolescente com proteção
     * específica (art. 14) e exige consentimento do responsável. Enquanto não existir um fluxo de
     * cadastro pelo responsável, a barreira é a resposta honesta — e a mensagem diz por onde o
     * menor de idade entra: pelo treinador, com o responsável junto.
     */
    const val MIN_AGE_YEARS = 16

    /** Dígitos de uma data completa, `DDMMAAAA`. */
    const val BIRTH_DATE_DIGITS = 8

    /** Celular brasileiro com DDD: 10 dígitos em fixo, 11 em móvel com o nono. */
    const val MAX_PHONE_DIGITS = 11

    fun validateEmail(email: String): EmailError? = when {
        email.isBlank() -> EmailError.REQUIRED
        !EMAIL_PATTERN.matches(email.trim()) -> EmailError.INVALID
        else -> null
    }

    fun validatePassword(password: String, requireMinLength: Boolean): PasswordError? = when {
        password.isEmpty() -> PasswordError.REQUIRED
        requireMinLength && password.length < MIN_PASSWORD_LENGTH -> PasswordError.TOO_SHORT
        else -> null
    }

    /**
     * Nome e sobrenome, porque é o treinador quem vai procurar por ele numa lista de alunos —
     * "Ana" sozinha não distingue ninguém numa carteira de trinta pessoas.
     *
     * Não exige acento, não recusa nome curto e não tenta adivinhar o que é "nome de verdade":
     * validação de nome que faz isso rejeita gente real, e o erro é caro porque acontece no
     * primeiro campo do cadastro.
     */
    fun validateName(name: String): NameError? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> NameError.REQUIRED
            trimmed.split(WHITESPACE).size < WORDS_IN_FULL_NAME -> NameError.INCOMPLETE
            else -> null
        }
    }

    /**
     * Data de nascimento a partir dos dígitos crus do campo mascarado.
     *
     * Distingue "ainda incompleta" de "inválida" de propósito: quem está no meio da digitação não
     * merece ouvir que a data não existe.
     *
     * @param today injetável para o teste não depender do dia em que roda.
     */
    fun validateBirthDate(digits: String, today: LocalDate = LocalDate.now()): BirthDateError? {
        // Data no futuro cai junto com data que não existe: as duas são erro de digitação, e a
        // idade calculada a partir delas sairia negativa.
        val date = parseBirthDate(digits)?.takeIf { !it.isAfter(today) }
        val age = date?.let { Period.between(it, today).years }

        return when {
            digits.isEmpty() -> BirthDateError.REQUIRED
            digits.length < BIRTH_DATE_DIGITS -> BirthDateError.INCOMPLETE
            age == null -> BirthDateError.INVALID
            age > MAX_AGE_YEARS -> BirthDateError.INVALID
            age < MIN_AGE_YEARS -> BirthDateError.TOO_YOUNG
            else -> null
        }
    }

    /**
     * Telefone é **opcional**, então vazio passa. Preenchido pela metade não passa: meio número
     * de telefone é pior do que nenhum, porque parece um canal de contato que não existe.
     */
    fun validatePhone(digits: String): PhoneError? = when {
        digits.isEmpty() -> null
        digits.length < MIN_PHONE_DIGITS || digits.length > MAX_PHONE_DIGITS -> PhoneError.INVALID
        else -> null
    }

    /**
     * `DDMMAAAA` para data, ou `null` quando os dígitos não formam um dia que existe.
     *
     * `LocalDate.of` recusa 31/02 e acerta ano bissexto — é por isso que a conversão passa por ele
     * em vez de comparar faixas de dia e mês na mão.
     */
    fun parseBirthDate(digits: String): LocalDate? {
        if (digits.length != BIRTH_DATE_DIGITS) return null

        return runCatching {
            LocalDate.of(
                digits.substring(MONTH_END).toInt(),
                digits.substring(DAY_END, MONTH_END).toInt(),
                digits.substring(0, DAY_END).toInt(),
            )
        }.getOrNull()
    }

    /** Acima disso é erro de digitação no ano, não longevidade. */
    private const val MAX_AGE_YEARS = 120

    private const val MIN_PHONE_DIGITS = 10
    private const val WORDS_IN_FULL_NAME = 2
    private const val DAY_END = 2
    private const val MONTH_END = 4

    private val WHITESPACE = Regex("\\s+")

    /**
     * Regex deliberadamente permissiva: algo@algo.algo. Validar e-mail com precisão é
     * notoriamente impossível, e recusar endereço válido é pior do que aceitar um inválido que o
     * servidor recusaria em seguida.
     */
    private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
}

internal enum class EmailError { REQUIRED, INVALID }

internal enum class PasswordError { REQUIRED, TOO_SHORT }

internal enum class NameError { REQUIRED, INCOMPLETE }

internal enum class BirthDateError { REQUIRED, INCOMPLETE, INVALID, TOO_YOUNG }

internal enum class PhoneError { INVALID }
