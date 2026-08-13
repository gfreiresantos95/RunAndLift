package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.runtime.Immutable
import com.gabrielfreire.runandlift.feature.auth.component.LegalDocument

/**
 * Eventos dos campos de perfil do cadastro.
 *
 * Separado de [SignUpActions] por duas razões: um contrato só passaria de dez lambdas, que é o
 * tamanho em que ninguém mais confere se todas foram ligadas; e são coisas de natureza diferente —
 * um lado autentica, o outro descreve quem é a pessoa.
 *
 * É também o contrato que a conclusão de cadastro reaproveita: lá a conta já existe, então nada de
 * [SignUpActions] se aplica, mas os blocos de campo são os mesmos.
 */
@Immutable
internal data class SignUpFormActions(
    val onNameChange: (String) -> Unit,
    val onBirthDateChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    /** Só é acionado no cadastro de treinador — para o aluno o campo nem chega a existir. */
    val onCrefChange: (String) -> Unit,
    val onTermsChange: (Boolean) -> Unit,
    val onMarketingChange: (Boolean) -> Unit,
    val onOpenLegalDocument: (LegalDocument) -> Unit,
)
