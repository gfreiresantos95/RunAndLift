package com.gabrielfreire.runandlift.feature.student.account

import com.gabrielfreire.runandlift.feature.student.validation.NameError
import com.gabrielfreire.runandlift.feature.student.validation.PhoneError

/**
 * Estado dos dados cadastrais do aluno.
 *
 * A tela edita **duas coisas**: nome e celular. As outras duas que ela mostra — e-mail e data de
 * nascimento — são leitura, e por motivos diferentes:
 *
 * - **E-mail** é a credencial de acesso. Trocá-lo exige reautenticação e confirmação no endereço
 *   novo, e é outro fluxo; um campo editável aqui prometeria uma troca que esta tela não sabe fazer.
 * - **Nascimento** decide a barreira de idade do cadastro, que tem base legal. Deixá-lo editável
 *   transformaria uma conta recusada por idade em uma conta aceita com dois toques.
 *
 * Mostrar os dois em vez de escondê-los é decisão de interface: campo ausente levanta a dúvida de
 * onde se troca aquilo, e a explicação embaixo responde antes de a pergunta ser feita.
 *
 * @param birthDate já formatado para leitura, ou vazio quando não há — a tela não formata data.
 */
internal data class AccountUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val failed: Boolean = false,
    val saved: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val birthDate: String = "",
    val nameError: NameError? = null,
    val phoneError: PhoneError? = null,
) {

    /** Nada pendente. Consultado depois de validar, nunca antes. */
    val isValid: Boolean get() = nameError == null && phoneError == null
}
