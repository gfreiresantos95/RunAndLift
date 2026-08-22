package com.gabrielfreire.runandlift.feature.student.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O treino que este aluno recebeu.
 *
 * Uma leitura na abertura e nenhuma depois. **O dia aberto não relê nada**: ele vem do mesmo estado,
 * porque a prescrição inteira — dias e exercícios — chega dentro de um documento só. É a regra 2 do
 * orçamento de leitura (§2.4) rendendo aqui o que prometia: abrir seis dias custa o mesmo que abrir
 * nenhum.
 *
 * **Só lê.** Prescrever é ato do treinador, e a regra do Firestore reserva a ele `update` e
 * `delete`; o registro do que foi executado é outra coleção (`sessions`, E6-02) e outra tela. Esta
 * aqui existe para responder uma pergunta só, que é a que se faz de pé na academia: o que eu faço
 * hoje.
 *
 * `activeAssignment` custa 1 leitura — na prática uma, porque o id do documento é
 * `{trainerId}_{studentId}` e um aluno tem um treinador de cada vez.
 */
internal class StudentWorkoutsViewModel(
    private val authRepository: AuthRepository,
    private val assignmentRepository: AssignmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentWorkoutsUiState())
    val uiState: StateFlow<StudentWorkoutsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê a prescrição. Chamado ao abrir a aba e no botão de tentar de novo. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            if (uid == null) {
                _uiState.update { it.copy(loading = false, failed = true) }
                return@launch
            }

            runCatching { assignmentRepository.activeAssignment(uid) }
                .onSuccess { found ->
                    _uiState.update { it.copy(loading = false, failed = false, assignment = found) }
                }
                // O treino que já estava na tela sobrevive: quem abriu a aba no vestiário sem sinal
                // continua vendo o que leu antes, em vez de perder o treino por uma releitura.
                .onFailure { _uiState.update { it.copy(loading = false, failed = true) } }
        }
    }
}
