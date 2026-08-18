package com.gabrielfreire.runandlift.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException

/**
 * A exceção do SDK virando [AuthFailure].
 *
 * Mora fora de [FirebaseAuthRepository] pela razão de sempre — lá estão as chamadas ao SDK, aqui a
 * decisão —, e essa separação vale ainda mais aqui por causa da **ordem**: as classes de exceção do
 * Firebase herdam umas das outras, e `FirebaseAuthWeakPasswordException` **é** uma
 * `FirebaseAuthInvalidCredentialsException`. Testar a ordem exige poder chamar a tradução com uma
 * exceção na mão, o que dentro de um `try/catch` privado não acontece.
 *
 * A tradução existe para a tela não conhecer código de erro do provedor: a UI escolhe a frase a
 * partir de um conjunto fechado, que não muda se um dia o provedor mudar. A exceção original segue
 * viva em `AuthResult.Failure.cause`, para a telemetria de E0-11.
 */
internal object AuthFailureMapping {

    /**
     * @return o motivo correspondente. Nunca lança, e nunca devolve `null`: o que não se sabe
     *   nomear é [AuthFailure.UNKNOWN], porque uma falha sem nome ainda precisa virar uma frase.
     */
    fun reasonFor(failure: Throwable): AuthFailure = when (failure) {
        // Antes de `InvalidCredentials`, e não por preferência: senha fraca **é** uma credencial
        // inválida para o SDK. Invertida a ordem, quem escolheu uma senha curta seria mandado
        // conferir o e-mail e a senha que digitou certos.
        is FirebaseAuthWeakPasswordException -> AuthFailure.WEAK_PASSWORD

        is FirebaseAuthUserCollisionException -> AuthFailure.EMAIL_ALREADY_IN_USE

        // Conta que não existe e senha errada dão a mesma resposta de propósito: distingui-las
        // transformaria a tela de entrada num verificador de quem tem cadastro aqui.
        is FirebaseAuthInvalidUserException -> AuthFailure.INVALID_CREDENTIALS

        is FirebaseAuthInvalidCredentialsException -> AuthFailure.INVALID_CREDENTIALS

        is FirebaseTooManyRequestsException -> AuthFailure.TOO_MANY_ATTEMPTS

        // As duas dizem a mesma coisa a quem está olhando a tela: não foi você, foi a rede.
        is FirebaseNetworkException -> AuthFailure.NO_NETWORK

        is IOException -> AuthFailure.NO_NETWORK

        else -> AuthFailure.UNKNOWN
    }
}
