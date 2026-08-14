package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.toDetails
import com.gabrielfreire.runandlift.feature.student.trainingform.validated
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * O passo a passo que apresenta o aluno ao treinador (backlog E2-01).
 *
 * Três decisões governam este fluxo:
 *
 * - **Uma gravação só, no fim.** Os passos mudam estado em memória e não tocam a rede; o documento
 *   é escrito uma vez, ao concluir. Gravar a cada passo custaria seis escritas para responder o que
 *   uma responde, e daria a cada tela um jeito próprio de falhar. Quem abandona no meio não gravou
 *   nada — e é a home que cobra, com o aviso de cadastro incompleto.
 * - **Pular é resposta.** Todo passo tem saída, inclusive o do consentimento. O que não foi
 *   respondido fica `null`, não é escrito, e volta como aviso. Um onboarding que prende a pessoa no
 *   passo dois é onde se perde quem ainda não sabe se vai usar o aplicativo.
 * - **Falha na gravação não prende.** A conta existe e o treino não depende disto: a tela avisa,
 *   deixa tentar de novo e deixa seguir. O que não foi gravado reaparece no aviso da home.
 *
 * A sequência **cresce** quando o aceite de dado de saúde é dado: peso, altura e restrições só
 * passam a existir como pergunta depois que há autorização para guardá-los.
 */
internal class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TrainingFormState())
    val formState: StateFlow<TrainingFormState> = _formState.asStateFlow()

    /**
     * Se a pergunta dos dias chegou a ser respondida.
     *
     * Existe porque conjunto vazio é ambíguo na gravação: "nenhum dia fixo" é resposta, "pulei" não
     * é. Sem esta marca, pular o passo gravaria um vazio como se fosse escolha.
     */
    private var daysAnswered = false

    fun onLevelSelect(level: TrainingLevel) {
        _formState.update { it.copy(level = level) }
    }

    fun onGoalSelect(goal: TrainingGoal) {
        _formState.update { it.copy(goal = goal) }
    }

    fun onDayToggle(day: DayOfWeek) {
        _formState.update { it.toggleDay(day) }
    }

    fun onWeightChange(value: String) {
        _formState.update { it.copy(weight = value, weightError = null) }
    }

    fun onHeightChange(value: String) {
        _formState.update { it.copy(height = value, heightError = null) }
    }

    fun onRestrictionsChange(value: String) {
        _formState.update { it.copy(restrictions = value) }
    }

    /**
     * O aceite do aviso de dado de saúde.
     *
     * Marcar **acrescenta** os dois passos seguintes; desmarcar os tira de novo, junto do que já
     * tiver sido digitado neles — dado sensível não fica em memória à espera de uma autorização que
     * foi retirada.
     */
    fun onHealthConsentChange(accepted: Boolean) {
        _formState.update {
            if (accepted) it.copy(healthConsent = true) else it.copy(healthConsent = false, weight = "", height = "")
        }
        _uiState.update { it.copy(total = OnboardingStep.sequenceFor(accepted).size) }
    }

    /**
     * O passo terminou — pelos dois botões, que fazem quase a mesma coisa.
     *
     * Uma função só porque a diferença entre "Continuar" e "Pular" é exatamente uma: [answered].
     * Ela decide se o passo dos dias conta como respondido e se os campos digitados são conferidos
     * antes de seguir. Duas funções repetiriam o resto, e é no resto que mora o avanço da sequência
     * e a gravação final.
     *
     * A validação roda **só nos campos digitados** — peso e altura. Escolha de lista não tem como
     * estar errada, e é por isso que os outros passos nunca barram ninguém. Quem pula não é
     * validado: um campo mal preenchido que a pessoa desistiu de responder não é motivo para
     * prendê-la nele.
     */
    fun onStepDone(answered: Boolean) {
        if (_uiState.value.saving) return

        if (answered && !_formState.updateAndGet { it.validated() }.isValid) return

        advance(answered)
    }

    private fun advance(answered: Boolean) {
        val current = _uiState.value
        val sequence = OnboardingStep.sequenceFor(_formState.value.healthConsent)

        if (current.step == OnboardingStep.DAYS && answered) daysAnswered = true

        if (current.position >= sequence.size) {
            save()
            return
        }

        _uiState.update {
            it.copy(step = sequence[current.position], position = current.position + 1, total = sequence.size)
        }
    }

    /**
     * A escrita única do fluxo.
     *
     * O consentimento vai junto **desta** gravação, e é ela que carimba o momento do aceite no
     * servidor. Falhar aqui não é motivo para segurar ninguém: o estado sai de `saving` com
     * `failed`, e a tela decide o que oferecer.
     */
    private fun save() {
        val uid = authRepository.currentAccountOrNull()?.uid

        if (uid == null) {
            _uiState.update { it.copy(finished = true) }
            return
        }

        _uiState.update { it.copy(saving = true, failed = false) }

        viewModelScope.launch {
            val form = _formState.value
            val saved = runCatching {
                studentRepository.save(
                    uid = uid,
                    details = form.toDetails(includeDays = daysAnswered, consentJustGiven = form.healthConsent),
                )
            }.isSuccess

            _uiState.update { it.copy(saving = false, failed = !saved, finished = saved) }
        }
    }
}
