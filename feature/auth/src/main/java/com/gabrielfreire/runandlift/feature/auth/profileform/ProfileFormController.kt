package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.data.model.BrazilState
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * O estado do formulário de perfil e tudo o que mexe nele.
 *
 * Existe porque **duas telas editam o mesmo formulário**: o cadastro por e-mail e a conclusão de
 * cadastro do Google. Antes desta classe os dois ViewModels carregavam as mesmas oito mutações
 * escritas duas vezes — e elas já tinham começado a divergir, com um filtrando os dígitos da data e
 * o outro não. Duas cópias de uma regra de entrada é uma cópia esperando para aceitar o que a outra
 * recusa.
 *
 * Não é um ViewModel: não tem escopo, não faz I/O e não sabe o que acontece no envio. É o estado do
 * formulário com dono, para que o ViewModel de cada tela fique com o que é dela — autenticar,
 * gravar, decidir para onde ir.
 *
 * A régua que confere o formulário continua sendo [validated], que é onde mora a diferença entre os
 * perfis; aqui ela é apenas aplicada.
 */
internal class ProfileFormController {

    private val _state = MutableStateFlow(ProfileFormState())
    val state: StateFlow<ProfileFormState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    /**
     * Guarda só dígitos, e no máximo os de uma data completa.
     *
     * A máscara já faz isso na tela, mas o estado não depende dela: é ele que a validação e a
     * gravação leem, e um campo que aceitasse `21/05/1990` faria as duas terem de limpá-lo de novo,
     * cada uma do seu jeito.
     */
    fun onBirthDateChange(digits: String) {
        _state.update {
            it.copy(
                birthDate = digits.filter(Char::isDigit).take(AuthFormValidation.BIRTH_DATE_DIGITS),
                birthDateError = null,
            )
        }
    }

    fun onPhoneChange(digits: String) {
        _state.update {
            it.copy(
                phone = digits.filter(Char::isDigit).take(AuthFormValidation.MAX_PHONE_DIGITS),
                phoneError = null,
            )
        }
    }

    /**
     * Guarda o que a máscara entregou, sem refiltrar: quem garante dígito onde é dígito, letra
     * maiúscula onde é letra e o tamanho máximo é o próprio campo. Refiltrar aqui seria uma segunda
     * regra de formato, que um dia discordaria da primeira.
     */
    fun onCrefChange(content: String) {
        _state.update { it.copy(cref = content, crefError = null) }
    }

    /**
     * Estado escolhido na lista.
     *
     * **Trocar o estado apaga a cidade.** A cidade escolhida pertencia ao estado anterior, e mantê-la
     * produziria um par que não existe — Campinas no Rio de Janeiro. Apagar é a única saída que não
     * exige a pessoa perceber sozinha que precisa escolher de novo.
     *
     * Reescolher o **mesmo** estado não apaga nada: abrir a lista e confirmar o que já estava lá não
     * é uma troca, e punir quem só foi conferir seria gratuito.
     */
    fun onStatePicked(uf: String, name: String) {
        _state.update { current ->
            val changed = current.stateUf != uf

            current.copy(
                stateUf = uf,
                stateName = name,
                stateError = null,
                city = if (changed) "" else current.city,
                cityError = null,
            )
        }
    }

    fun onCityPicked(city: String) {
        _state.update { it.copy(city = city, cityError = null) }
    }

    fun onTermsChange(accepted: Boolean) {
        _state.update { it.copy(acceptedTerms = accepted, termsMissing = false) }
    }

    fun onMarketingChange(optIn: Boolean) {
        _state.update { it.copy(marketingOptIn = optIn) }
    }

    /**
     * Confere o formulário e devolve o resultado já com os erros preenchidos.
     *
     * Devolve em vez de só marcar porque quem chamou precisa decidir na hora se envia — e reler o
     * `StateFlow` logo depois de atualizá-lo é uma corrida esperando para acontecer.
     */
    fun validate(isTrainer: Boolean, askName: Boolean = true, askConsent: Boolean = true): ProfileFormState =
        _state.updateAndGet { it.validated(isTrainer = isTrainer, askName = askName, askConsent = askConsent) }

    /** Traz para os campos o que já está gravado. Ver [prefilledFrom]. */
    fun prefill(profile: UserProfile?, registration: String?, state: BrazilState?) {
        _state.update { it.prefilledFrom(profile = profile, registration = registration, state = state) }
    }
}
