package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Escolha de papel **depois** de autenticar (backlog E1-02).
 *
 * Desde que existe [WelcomeScreen], esta tela é a rede de segurança, não o caminho comum: quem
 * passa pelas boas-vindas chega ao papel já gravado pelo cadastro. Ela ainda é alcançada por conta
 * que existe sem papel — sessão anterior à escolha, primeiro login com Google feito pela tela de
 * entrar, ou gravação que falhou no cadastro.
 *
 * Grava o papel em `users/{uid}` **somando** ao que já existir, nunca substituindo: é o que
 * permite a mesma conta ser treinador e aluno de outro treinador, sem segundo login (§3.2).
 */
internal class RoleSelectionViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleSelectionUiState())
    val uiState: StateFlow<RoleSelectionUiState> = _uiState.asStateFlow()

    fun onSelect(role: ActiveRole) {
        _uiState.update { it.copy(selected = role, failed = false) }
    }

    fun onConfirm() {
        val current = _uiState.value
        val role = current.selected
        val account = authRepository.currentAccountOrNull()

        // Sem papel escolhido ou já enviando: nada a fazer, e nada a sinalizar.
        if (role == null || current.submitting) return

        // Sem conta é falha de verdade — significa que a sessão caiu entre a tela anterior e esta.
        if (account == null) {
            _uiState.update { it.copy(failed = true) }
            return
        }

        _uiState.update { it.copy(submitting = true, failed = false) }

        viewModelScope.launch {
            runCatching {
                userRepository.saveProfile(
                    uid = account.uid,
                    role = role,
                    // Nome derivado do e-mail só entra se ainda não houver um: quem passou pelo
                    // formulário de cadastro já informou o nome real, e o repositório preserva.
                    details = SignUpDetails(displayName = account.email?.substringBefore('@')),
                )
            }.onSuccess { profile ->
                _uiState.update { it.copy(submitting = false, confirmedRole = profile.activeRole) }
            }.onFailure {
                _uiState.update { it.copy(submitting = false, failed = true) }
            }
        }
    }
}
