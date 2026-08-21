package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * O programa que está sendo montado, e tudo o que mexe nele.
 *
 * Existe pela mesma razão de `ProfileFormController` no cadastro: **três telas editam o mesmo
 * objeto** — o programa, o dia e a prescrição —, e sem um dono as onze mutações estariam espalhadas
 * pelo ViewModel junto com carregar e gravar, o que já passa do limite de responsabilidade que o
 * projeto se impôs.
 *
 * Não é um ViewModel: não tem escopo, não faz I/O e não sabe o que acontece ao salvar. É o rascunho
 * com dono, para o ViewModel ficar com o que é dele — ler, gravar e dizer se deu certo.
 *
 * **As regras de mudança não estão aqui**, estão em `ProgramEdits.kt`, como funções puras. Este
 * arquivo é só quem as aplica ao estado; é lá que um teste comum alcança o que acontece quando um
 * índice não existe mais.
 */
internal class ProgramDraftController {

    private val _draft = MutableStateFlow(Program(id = "", trainerId = "", name = ""))
    val draft: StateFlow<Program> = _draft.asStateFlow()

    /**
     * O rascunho atual. Escrever aqui **substitui tudo** — é o que o ViewModel faz ao terminar de
     * ler do Firestore e ao receber de volta o programa recém-gravado, já com id.
     */
    var current: Program
        get() = _draft.value
        set(value) {
            _draft.value = value
        }

    fun onNameChange(name: String) {
        _draft.update { it.copy(name = name) }
    }

    /**
     * Troca o objetivo, ou o desmarca.
     *
     * Tocar no objetivo já escolhido o remove, e isso é deliberado: são chips de escolha única, e
     * sem essa saída um objetivo marcado por engano no primeiro toque não teria como ser desfeito.
     */
    fun onGoalChange(goal: TrainingGoal?) {
        _draft.update { it.copy(goal = if (it.goal == goal) null else goal) }
    }

    fun onNotesChange(notes: String) {
        _draft.update { it.copy(notes = notes) }
    }

    fun onAddDay() {
        _draft.update { it.withDayAdded() }
    }

    fun onRemoveDay(dayIndex: Int) {
        _draft.update { it.withDayRemoved(dayIndex) }
    }

    fun onDayInfoChange(dayIndex: Int, label: String, focus: String) {
        _draft.update { it.withDayInfo(dayIndex, label, focus) }
    }

    fun onAddExercise(dayIndex: Int, exercise: Exercise) {
        _draft.update { it.withExerciseAdded(dayIndex, exercise) }
    }

    fun onRemoveExercise(dayIndex: Int, exerciseIndex: Int) {
        _draft.update { it.withExerciseRemoved(dayIndex, exerciseIndex) }
    }

    fun onMoveExercise(dayIndex: Int, from: Int, to: Int) {
        _draft.update { it.withExerciseMoved(dayIndex, from, to) }
    }

    fun onPrescriptionChange(dayIndex: Int, exerciseIndex: Int, prescription: PrescribedExercise) {
        _draft.update { it.withPrescription(dayIndex, exerciseIndex, prescription) }
    }
}
