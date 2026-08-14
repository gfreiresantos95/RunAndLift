package com.gabrielfreire.runandlift.feature.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidade de quem abriu a home.
 *
 * Lê o perfil uma vez, na criação. Não observa: nome de usuário não muda enquanto a tela está
 * aberta, e um `Flow` vivo aqui custaria um listener no Firestore em uma tela que abre toda vez
 * que o app abre — exatamente o que a regra 4 do orçamento de leitura evita (§2.4).
 *
 * **Leitura que falha não vira erro de tela.** Sem rede e sem cache, a home aparece com a saudação
 * sem nome; ela não é uma tela que exige o dado para funcionar. Pintar um banner de falha aqui
 * ensinaria a ignorar o banner que importa.
 */
internal class StudentHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid
            val name = uid
                ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
                ?.displayName
                ?.takeIf { it.isNotBlank() }

            _uiState.value = StudentHomeUiState(loading = false, displayName = name)
        }
    }
}
