package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidade de quem abriu a home do treinador.
 *
 * Uma leitura, na criação, sem observar — nome não muda com a tela aberta, e um listener aqui
 * custaria uma assinatura no Firestore na tela que abre toda vez que o app abre (§2.4, regra 4).
 *
 * Leitura que falha não vira erro de tela: a home aparece com a saudação sem nome.
 */
internal class TrainerHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerHomeUiState())
    val uiState: StateFlow<TrainerHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid
            val name = uid
                ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
                ?.displayName
                ?.takeIf { it.isNotBlank() }

            _uiState.value = TrainerHomeUiState(loading = false, displayName = name)
        }
    }
}
