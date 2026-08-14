package com.gabrielfreire.runandlift.feature.auth.recovery

import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.feature.auth.validation.EmailError

/**
 * Estado da recuperação de senha.
 *
 * [sent] é `true` **inclusive quando o e-mail não existe**, e isso não é um descuido: ver
 * [PasswordRecoveryViewModel]. [failure] fica reservado a falha de verdade — rede, servidor,
 * excesso de tentativas —, que é o único caso em que tentar de novo muda alguma coisa.
 */
internal data class PasswordRecoveryUiState(
    val email: String = "",
    val emailError: EmailError? = null,
    val failure: AuthFailure? = null,
    val submitting: Boolean = false,
    val sent: Boolean = false,
)
