package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.Link

/**
 * Desfecho de pedir vínculo a um treinador.
 *
 * Resultado, e não exceção, pela mesma razão de `AuthResult`: aqui a falha é esperada — pedir duas
 * vezes é o caso comum, não o excepcional — e o que a tela precisa é escolher uma frase, não
 * capturar um `Throwable`.
 */
sealed interface LinkRequestResult {

    /** O pedido foi criado e está esperando o treinador confirmar. */
    data class Success(val link: Link) : LinkRequestResult

    /**
     * Falha com causa identificada. [cause] guarda a exceção original para a telemetria de E0-11; a
     * tela escolhe a mensagem por [reason] e não deve tocar nela.
     */
    data class Failure(val reason: LinkRequestFailure, val cause: Throwable? = null) : LinkRequestResult
}
