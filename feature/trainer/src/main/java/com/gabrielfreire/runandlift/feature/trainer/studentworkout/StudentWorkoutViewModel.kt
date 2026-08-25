package com.gabrielfreire.runandlift.feature.trainer.studentworkout

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
 * O que este treinador prescreveu para este aluno.
 *
 * **Uma leitura na abertura e nenhuma depois.** O treino inteiro — nome do programa, objetivo,
 * observação, dias e exercícios — chega dentro de um documento só, que é o que a cópia congelada em
 * `assignments` existe para permitir. Abrir todos os dias custa o mesmo que abrir nenhum, e é a
 * regra 2 do orçamento de leitura (§2.4) rendendo do lado do treinador o que já rendia do lado do
 * aluno.
 *
 * **Só lê.** Editar a prescrição continua sendo pelo editor de programa e pela atribuição — esta
 * tela responde uma pergunta e só: o que o meu aluno está com a mão.
 *
 * Falha de leitura não vira "este aluno não tem treino". A frase errada aqui manda o treinador
 * prescrever de novo o que ele já prescreveu, e a reatribuição substitui a cópia que o aluno estava
 * usando — é o tipo de engano que a rede fraca produz e que a tela não pode ajudar a produzir.
 */
internal class StudentWorkoutViewModel(
    private val authRepository: AuthRepository,
    private val assignmentRepository: AssignmentRepository,
    private val studentId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentWorkoutUiState())
    val uiState: StateFlow<StudentWorkoutUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê a prescrição. Chamado ao abrir, ao voltar para a tela e no botão de tentar de novo. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            if (uid == null) {
                _uiState.update { it.copy(loading = false, failed = true) }
                return@launch
            }

            runCatching { assignmentRepository.assignmentOf(trainerId = uid, studentId = studentId) }
                .onSuccess { found ->
                    _uiState.update { it.copy(loading = false, failed = false, assignment = found) }
                }
                // O treino que já estava na tela sobrevive à releitura que não respondeu: quem
                // acabou de vê-lo não o perde por causa do sinal.
                .onFailure { _uiState.update { it.copy(loading = false, failed = true) } }
        }
    }
}
