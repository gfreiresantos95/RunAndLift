package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.trainer.profile.MissingTrainerData
import com.gabrielfreire.runandlift.feature.trainer.profile.TrainerProfileCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidade de quem abriu a home do treinador, e o que falta no perfil profissional dele.
 *
 * Lê os dois documentos uma vez, na criação, e **não observa**: nem o nome nem o perfil mudam
 * enquanto a tela está aberta, e um `Flow` vivo aqui custaria dois listeners no Firestore na tela
 * que abre toda vez que o app abre — o oposto da regra 4 do orçamento (§2.4).
 *
 * Por não observar, a home é recarregada ao voltar da edição de perfil: quem corrigiu um dado
 * precisa ver o aviso sumir. É [refresh] que faz isso, e ele é chamado pela navegação, não por um
 * temporizador.
 *
 * **Leitura que falha não vira erro de tela.** Sem rede e sem cache, a home aparece com a saudação
 * sem nome e sem aviso — e "sem aviso" é a resposta certa: acusar cadastro incompleto por causa de
 * uma leitura que não respondeu é um palpite, e treina a pessoa a ignorar avisos.
 */
internal class TrainerHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val trainerRepository: TrainerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerHomeUiState())
    val uiState: StateFlow<TrainerHomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê o que a tela mostra. Chamado ao abrir e ao voltar da edição de perfil. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            val name = uid
                ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
                ?.displayName
                ?.takeIf { it.isNotBlank() }

            val missing = uid
                ?.let { TrainerProfileCompletion.missing(trainerRepository, it) }
                ?: MissingTrainerData()

            _uiState.value = TrainerHomeUiState(loading = false, displayName = name, missing = missing)
        }
    }
}
