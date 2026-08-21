package com.gabrielfreire.runandlift.feature.trainer.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.data.program.ProgramRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Quem pode receber este programa, e quem já o tem.
 *
 * Lê três coisas na abertura e nenhuma delas depois: o programa (1 leitura), a carteira de alunos
 * (1 por vínculo) e quem já está com este programa (1 por atribuição). É a tela mais cara da
 * montagem, e é aceitável porque se abre uma vez por prescrição — não é a home.
 *
 * **Atribuir congela uma cópia dos dias**, e é aqui que isso acontece: [Assignment.from] transforma
 * o molde na prescrição de uma pessoa. Depois disso os dois são objetos independentes, e é o que
 * permite ao aluno ler o próprio treino sem poder ler a coleção de programas.
 */
internal class AssignViewModel(
    private val authRepository: AuthRepository,
    private val linkRepository: LinkRepository,
    private val programRepository: ProgramRepository,
    private val assignmentRepository: AssignmentRepository,
    private val programId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignUiState())
    val uiState: StateFlow<AssignUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê programa, carteira e atribuições. Chamado ao abrir e no botão de tentar de novo. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            if (uid == null) {
                _uiState.update { it.copy(loading = false, failed = true) }
                return@launch
            }

            runCatching { load(uid) }
                .onSuccess { loaded -> _uiState.value = loaded }
                .onFailure { _uiState.update { it.copy(loading = false, failed = true) } }
        }
    }

    private suspend fun load(uid: String): AssignUiState {
        val program = programRepository.program(programId)
        val links = linkRepository.trainerLinks(uid).filter { it.status == LinkStatus.ACTIVE }
        val assigned = assignmentRepository
            .assignmentsOfProgram(trainerId = uid, programId = programId)
            .filter { it.isActive }
            .map { it.studentId }
            .toSet()

        return AssignUiState(
            loading = false,
            program = program,
            students = links.sortedBy { it.studentName.lowercase() },
            assignedIds = assigned,
        )
    }

    /**
     * Atribui o programa a um aluno.
     *
     * **Substitui o treino anterior daquele aluno com este treinador**, porque o id do documento é
     * `{trainerId}_{studentId}`. É o comportamento certo para o produto — o aluno tem *o* treino,
     * no singular — e é o que a tela precisa dizer antes de gravar. Ver [Assignment].
     *
     * Falha vira aviso e a linha volta ao que era. Sem rede não há como prescrever: a fila durável
     * (E0-04) ainda não existe, e fingir que gravou seria pior do que recusar.
     */
    fun onAssign(link: Link) {
        val program = _uiState.value.program ?: return
        if (_uiState.value.assigning != null) return

        // A trava é levantada **antes** do `launch`, e não dentro dele, como em `StudentsViewModel`:
        // marcá-la lá faria os dois toques de um toque duplo passarem pela verificação antes de
        // qualquer um deles a acender, e a proteção passaria a depender de o `Main.immediate` rodar
        // o corpo da corrotina na hora — que é verdade no aparelho e não é garantia.
        _uiState.update { it.copy(assigning = link.studentId, assignFailed = false) }

        viewModelScope.launch {
            val assignment = Assignment.from(
                program = program,
                studentId = link.studentId,
                studentName = link.studentName,
            )

            runCatching { assignmentRepository.assign(assignment) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(assigning = null, assignedIds = state.assignedIds + link.studentId)
                    }
                }
                .onFailure { _uiState.update { it.copy(assigning = null, assignFailed = true) } }
        }
    }

    /** Encerra a prescrição deste aluno. O documento fica, com a situação trocada. */
    fun onRemove(link: Link) {
        val program = _uiState.value.program ?: return
        if (_uiState.value.assigning != null) return

        _uiState.update { it.copy(assigning = link.studentId, assignFailed = false) }

        viewModelScope.launch {
            val assignment = Assignment.from(
                program = program,
                studentId = link.studentId,
                studentName = link.studentName,
            )

            runCatching { assignmentRepository.end(assignment) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(assigning = null, assignedIds = state.assignedIds - link.studentId)
                    }
                }
                .onFailure { _uiState.update { it.copy(assigning = null, assignFailed = true) } }
        }
    }
}
