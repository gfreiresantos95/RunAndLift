package com.gabrielfreire.runandlift.feature.student.account

import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.feature.student.validation.CityError
import com.gabrielfreire.runandlift.feature.student.validation.NameError
import com.gabrielfreire.runandlift.feature.student.validation.PhoneError
import com.gabrielfreire.runandlift.feature.student.validation.StateError

/**
 * Estado dos dados cadastrais do aluno.
 *
 * A tela edita **quatro coisas**: nome, celular, estado e cidade. As outras duas que ela mostra —
 * e-mail e data de nascimento — são leitura, e por motivos diferentes:
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
 * @param stateUf sigla do estado — é o que vai para o banco.
 * @param stateName nome por extenso do mesmo estado, guardado **só para desenhar o campo**: a tela
 *   exibe "São Paulo - SP" e o banco guarda "SP". Ver
 *   [com.gabrielfreire.runandlift.data.model.BrazilState].
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
    val stateUf: String = "",
    val stateName: String = "",
    val city: String = "",
    val nameError: NameError? = null,
    val phoneError: PhoneError? = null,
    val stateError: StateError? = null,
    val cityError: CityError? = null,
) {

    /**
     * As duas metades do estado remontadas, ou `null` enquanto não houver escolha.
     *
     * A costura fica num lugar só: feita em cada ponto que precisa dela, seria a chance de um deles
     * inverter a ordem e a escolha parecer ter mudado sozinha.
     */
    val selectedState: BrazilState?
        get() = stateUf.takeIf { it.isNotEmpty() }?.let { BrazilState(uf = it, name = stateName) }

    /** Nada pendente. Consultado depois de validar, nunca antes. */
    val isValid: Boolean
        get() = nameError == null && phoneError == null && stateError == null && cityError == null
}
