package com.gabrielfreire.runandlift.feature.trainer.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.program.ProgramRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Os programas do treinador logado.
 *
 * Relê a cada volta para a aba, e não só na criação: a tela ao lado é o editor, e voltar dele sem a
 * lista atualizada mostraria o programa recém-salvo faltando — ou, pior, o recém-apagado presente.
 *
 * **Falha de leitura não apaga o que já está na tela.** `failed = true` entra e a lista fica como
 * estava; quem abriu a aba no metrô continua vendo os programas de antes em vez de uma tela em
 * branco. É a mesma política de `StudentsViewModel`.
 */
internal class ProgramsViewModel(
    private val authRepository: AuthRepository,
    private val programRepository: ProgramRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramsUiState())
    val uiState: StateFlow<ProgramsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê a lista. Chamado ao abrir e ao voltar do editor. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            if (uid == null) {
                _uiState.update { it.copy(loading = false, failed = true) }
                return@launch
            }

            runCatching { programRepository.programs(uid) }
                .onSuccess { programs ->
                    _uiState.update {
                        it.copy(loading = false, failed = false, programs = programs)
                    }
                }
                .onFailure { _uiState.update { it.copy(loading = false, failed = true) } }
        }
    }

    /**
     * Apaga um molde.
     *
     * **Não alcança quem já recebeu**: as atribuições carregam a própria cópia dos dias, então
     * nenhum aluno fica sem treino por causa disto. É o outro lado de a cópia ser congelada, e é o
     * que permite a exclusão ser uma operação simples em vez de uma cascata.
     *
     * Falha deixa a lista como está e acende [ProgramsUiState.failed] — o programa continua ali,
     * que é a verdade.
     */
    fun onDelete(program: Program) {
        if (_uiState.value.deleting != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(deleting = program.id) }

            runCatching { programRepository.delete(program.id) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            deleting = null,
                            programs = state.programs.filterNot { it.id == program.id },
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(deleting = null, failed = true) } }
        }
    }
}
