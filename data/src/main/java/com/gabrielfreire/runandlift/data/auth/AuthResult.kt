package com.gabrielfreire.runandlift.data.auth

import com.gabrielfreire.runandlift.data.model.UserAccount

/** Desfecho de uma operação de autenticação. */
sealed interface AuthResult {

    data class Success(val account: UserAccount?) : AuthResult

    /**
     * Falha esperada, com causa identificada — a UI escolhe a mensagem a partir de [reason].
     * [cause] guarda a exceção original para a telemetria de E0-11; a tela não deve usá-la.
     */
    data class Failure(val reason: AuthFailure, val cause: Throwable? = null) : AuthResult
}
