package com.gabrielfreire.runandlift.feature.trainer.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O código de convite do treinador: ler o que existe, e criar quando não existe.
 *
 * **Não gera código sozinho ao abrir a tela.** Seria um toque a menos no caminho principal e uma
 * gravação em nome de quem só quis olhar — e, para quem já tem código, um segundo de dúvida sobre
 * qual dos dois vale. Gerar é decisão, e por isso tem botão.
 *
 * O nome do treinador é lido junto porque **viaja para dentro do convite**: é com ele que o aluno
 * confere com quem está se vinculando antes de pedir. Nome que não veio não impede a geração: um
 * código sem nome ainda funciona, e travar a criação por causa disso seria transformar um cadastro
 * incompleto em uma porta fechada.
 */
internal class InviteViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState: StateFlow<InviteUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid
            val code = uid?.let { runCatching { linkRepository.inviteCode(it) } }

            _uiState.value = InviteUiState(
                loading = false,
                failed = code == null || code.isFailure,
                code = code?.getOrNull(),
            )
        }
    }

    /**
     * Cria um código e **descarta o anterior**, que é o que "gerar outro" significa.
     *
     * Quem toca aqui com um código já em circulação está justamente querendo invalidá-lo. A tela
     * avisa disso antes; o repositório é quem garante que os dois não fiquem valendo juntos.
     */
    fun onGenerate() {
        if (_uiState.value.working) return

        _uiState.update { it.copy(working = true, failed = false) }

        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid
            val name = uid
                ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
                ?.displayName
                .orEmpty()

            val code = uid?.let { runCatching { linkRepository.createInviteCode(it, name) }.getOrNull() }

            _uiState.update {
                it.copy(working = false, failed = code == null, code = code ?: it.code)
            }
        }
    }
}
