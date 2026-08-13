package com.gabrielfreire.runandlift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.completeprofile.ProfileCompletion
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.navigation.RoleRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de abertura: decide para onde o app vai antes de a splash sair.
 *
 * Quatro desfechos possíveis, e a ordem importa:
 * 1. sem sessão -> fluxo de entrada;
 * 2. com sessão e sem papel -> escolha de papel, porque a conta existe mas não sabe o que é;
 * 3. com sessão, com papel e cadastro pela metade -> conclusão de cadastro;
 * 4. com sessão e cadastro completo -> direto para o grafo do papel.
 *
 * O terceiro caso é o que impede que **fechar o app** vire a forma de pular o que a conclusão de
 * cadastro pergunta. Sem ele, quem entra pelo Google e mata o aplicativo na tela de conclusão volta
 * direto para a home — sem data de nascimento, sem aceite dos termos e, se for treinador, sem o
 * registro que a lei exige de quem prescreve.
 *
 * Resolver isso aqui, e não depois da primeira composição, é o que evita o app abrir na tela de
 * login e trocar para a home um frame depois — o piscar que o backlog quer evitar na abertura.
 */
class MainViewModel(private val authRepository: AuthRepository, private val userRepository: UserRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val account = authRepository.currentAccountOrNull()
            // Perfil vem do cache do Firestore quando existe, então abrir offline com sessão
            // ativa continua funcionando.
            val profile = account?.let { runCatching { userRepository.profile(it.uid) }.getOrNull() }
            val role = profile?.activeRole

            // Custa 0 leitura com o cache quente, que é o caso de toda abertura depois da
            // primeira. Leitura que falha responde "está completo", então rede ruim nunca segura
            // ninguém na porta.
            val incomplete = account != null && role != null &&
                ProfileCompletion.missing(userRepository, account.uid, role).any

            _uiState.value = MainUiState(
                ready = true,
                startDestination = startDestinationFor(account != null, role, incomplete),
                activeRole = role,
                canSwitchRole = profile?.roles?.hasBoth == true,
            )
        }
    }

    /** Troca o papel ativo e devolve o novo, ou `null` se a troca não se aplica. */
    fun switchRole(onSwitched: (ActiveRole) -> Unit) {
        val current = _uiState.value
        val target = when (current.activeRole) {
            ActiveRole.TRAINER -> ActiveRole.STUDENT
            ActiveRole.STUDENT -> ActiveRole.TRAINER
            null -> return
        }
        if (!current.canSwitchRole) return

        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid ?: return@launch
            runCatching { userRepository.setActiveRole(uid, target) }
                .onSuccess {
                    _uiState.value = current.copy(activeRole = target)
                    onSwitched(target)
                }
        }
    }

    private fun startDestinationFor(hasAccount: Boolean, role: ActiveRole?, incomplete: Boolean): String = when {
        !hasAccount -> AuthRoutes.GRAPH
        role == null -> AuthRoutes.ROLE_SELECTION
        incomplete -> AuthRoutes.completeProfile(role)
        else -> RoleRoutes.graphFor(role)
    }
}
