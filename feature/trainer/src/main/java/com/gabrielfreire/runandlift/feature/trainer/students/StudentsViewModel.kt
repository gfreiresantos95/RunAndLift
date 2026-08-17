package com.gabrielfreire.runandlift.feature.trainer.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A carteira de alunos e as decisões que ela pede.
 *
 * **Relê ao voltar para a aba, e não só na criação.** É a diferença desta tela para a home: o que
 * ela mostra muda quando *outra pessoa* age — um aluno digita o código agora e o pedido tem de
 * aparecer —, e o ViewModel sobrevive à troca de abas. Sem [refresh] chamado pela navegação, a
 * carteira ficaria congelada no estado de quando o app abriu.
 *
 * **Leitura que falha vira `failed`, e não lista vazia.** Aqui a distinção é obrigatória: "você
 * ainda não tem alunos" e "não consegui carregar" desenham a mesma tela em branco, e a primeira
 * frase dita a um treinador com trinta alunos é um susto — além de esconder que basta tentar de
 * novo.
 *
 * A mudança de estado é **otimista no que já se sabe e honesta no que falhou**: a lista é relida
 * depois de cada transição, porque a resposta do servidor é a única que vale — as regras podem
 * recusar uma transição que a tela achava possível.
 */
internal class StudentsViewModel(
    private val authRepository: AuthRepository,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsUiState())
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê a carteira. Chamado ao abrir a aba e ao voltar para ela. */
    fun refresh() {
        viewModelScope.launch { load() }
    }

    /**
     * Aceitar, recusar, pausar, retomar e encerrar — todas passam por aqui.
     *
     * Uma função para as cinco, e não uma por ação, porque o que muda entre elas é só o estado de
     * destino: quem decide se a transição é permitida são as Security Rules, e repetir a máquina de
     * estados aqui daria duas versões dela para divergirem.
     */
    fun onStatusChange(link: Link, status: LinkStatus) {
        if (_uiState.value.updating != null) return

        _uiState.update { it.copy(updating = link.studentId, failed = false) }

        viewModelScope.launch {
            val changed = runCatching { linkRepository.updateStatus(link, status) }.isSuccess

            // Recarrega em vez de trocar o item em memória: a transição recusada pelo servidor
            // deixaria a tela mostrando um estado que o banco não tem.
            load(failedOverride = !changed)
        }
    }

    private suspend fun load(failedOverride: Boolean = false) {
        val uid = authRepository.currentAccountOrNull()?.uid
        val links = uid?.let { runCatching { linkRepository.trainerLinks(it) }.getOrNull() }

        _uiState.value = StudentsUiState(
            loading = false,
            failed = failedOverride || links == null,
            // A lista antiga sobrevive à falha: quem já tinha a carteira na tela não a perde porque
            // uma releitura não respondeu.
            links = links ?: _uiState.value.links,
        )
    }
}
