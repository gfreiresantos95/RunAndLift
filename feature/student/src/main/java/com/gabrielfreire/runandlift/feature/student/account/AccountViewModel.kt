package com.gabrielfreire.runandlift.feature.student.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.student.validation.AccountFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dados cadastrais do aluno — o que está em `users/{uid}`.
 *
 * Separado do perfil de treino porque são **dois documentos com dois públicos**: isto aqui só o
 * titular lê, e o perfil de treino o treinador vinculado também lê. Uma tela só para os dois
 * esconderia essa diferença justamente de quem precisa entendê-la ao decidir o que preencher.
 *
 * Grava com `role = null`: mexer no nome não pode mexer no papel da conta.
 */
internal class AccountViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }

    fun onPhoneChange(digits: String) = _uiState.update {
        it.copy(phone = digits.filter(Char::isDigit).take(AccountFormValidation.MAX_PHONE_DIGITS), phoneError = null)
    }

    /**
     * Estado escolhido na lista.
     *
     * **Trocar o estado apaga a cidade.** A cidade escolhida pertencia ao estado anterior, e mantê-la
     * produziria um par que não existe — Campinas no Rio de Janeiro.
     */
    fun onStatePicked(uf: String, name: String) = _uiState.update { current ->
        val changed = current.stateUf != uf

        current.copy(
            stateUf = uf,
            stateName = name,
            stateError = null,
            city = if (changed) "" else current.city,
            cityError = null,
        )
    }

    fun onCityPicked(city: String) = _uiState.update { it.copy(city = city, cityError = null) }

    /** A tela já mostrou a confirmação. Ver `AccountActions.onSavedShown`. */
    fun onSavedShown() = _uiState.update { it.copy(saved = false) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.saving) return

        val validated = _uiState.updateAndGet {
            it.copy(
                nameError = AccountFormValidation.validateName(it.name),
                phoneError = AccountFormValidation.validatePhone(it.phone),
                stateError = AccountFormValidation.validateState(it.stateUf),
                cityError = AccountFormValidation.validateCity(it.city),
            )
        }
        if (!validated.isValid) return

        _uiState.update { it.copy(saving = true, failed = false) }
        viewModelScope.launch { save(validated) }
    }

    private suspend fun load() {
        val account = authRepository.currentAccountOrNull()
        val profile = account?.uid?.let { runCatching { userRepository.profile(it) }.getOrNull() }

        // O banco guarda só a sigla, e o campo exibe "São Paulo - SP": o nome vem daqui, uma vez,
        // na mesma carga que traz o resto. `state()` nunca falha — sem lista, devolve nulo, o campo
        // fica vazio e a pessoa escolhe de novo. Pedir de novo é melhor que exibir "- SP".
        val state = profile?.state?.let { locationRepository.state(it) }

        _uiState.update {
            it.copy(
                loading = false,
                name = profile?.displayName.orEmpty(),
                phone = profile?.phone.orEmpty(),
                email = account?.email.orEmpty(),
                // Formatado aqui, e não na tela: `Locale.ROOT` porque é data de leitura em formato
                // brasileiro fixo, e não texto que deva seguir o idioma do aparelho.
                birthDate = profile?.birthDate?.format(BIRTH_DATE_FORMAT).orEmpty(),
                stateUf = state?.uf.orEmpty(),
                stateName = state?.name.orEmpty(),
                // A cidade só volta acompanhada do estado: sozinha ela não identifica lugar nenhum,
                // e o campo ficaria preenchido com um nome que a lista não teria como confirmar.
                city = profile?.city.orEmpty().takeIf { state != null }.orEmpty(),
            )
        }
    }

    private suspend fun save(state: AccountUiState) {
        val uid = authRepository.currentAccountOrNull()?.uid

        // `updateIdentity` e não `saveProfile`: aquela preenche o que falta e **preserva** o nome
        // existente, que é o certo no cadastro e faria o botão de salvar não fazer nada aqui.
        val saved = uid != null && runCatching {
            userRepository.updateIdentity(
                uid = uid,
                displayName = state.name.trim(),
                phone = state.phone.ifEmpty { null },
                // Só a sigla atravessa. `stateName` existe para desenhar o campo e morre aqui —
                // gravá-lo seria uma segunda grafia do mesmo estado, livre para divergir.
                state = state.stateUf.ifEmpty { null },
                city = state.city.ifEmpty { null },
            )
        }.isSuccess

        _uiState.update { it.copy(saving = false, failed = !saved, saved = saved) }
    }

    private companion object {
        val BIRTH_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT)
    }
}
