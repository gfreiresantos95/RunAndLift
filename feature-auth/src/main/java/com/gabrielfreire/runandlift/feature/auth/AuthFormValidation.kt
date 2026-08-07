package com.gabrielfreire.runandlift.feature.auth

/**
 * Validação de formulário, separada dos ViewModels para ser testável sem Android.
 *
 * Valida apenas o que dá para saber sem ir ao servidor: campo vazio e formato. Se a senha está
 * correta ou o e-mail existe, quem responde é o servidor — validar isso aqui seria adivinhar.
 */
internal object AuthFormValidation {

    /** Piso do próprio Firebase Auth. Repetido aqui para o erro aparecer antes da ida à rede. */
    const val MIN_PASSWORD_LENGTH = 6

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
     * Regex deliberadamente permissiva: algo@algo.algo. Validar e-mail com precisão é
     * notoriamente impossível, e recusar endereço válido é pior do que aceitar um inválido que o
     * servidor recusaria em seguida.
     */
    private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
}

internal enum class EmailError { REQUIRED, INVALID }

internal enum class PasswordError { REQUIRED, TOO_SHORT }
