package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.toDetails
import com.gabrielfreire.runandlift.feature.student.trainingform.trainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.validated
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * O passo a passo que apresenta o aluno ao treinador (backlog E2-01).
 *
 * Abre **logo depois de a conta ser criada**, antes da home. Deixá-lo para a abertura seguinte era
 * tarde demais: a pessoa já teria visto a home vazia e formado a impressão de que não há nada a
 * fazer ali.
 *
 * Três decisões governam o fluxo:
 *
 * - **Uma gravação só, no fim.** Os passos mudam estado em memória e não tocam a rede; o documento
 *   é escrito uma vez, ao concluir. Gravar a cada passo custaria seis escritas para responder o que
 *   uma responde, e daria a cada tela um jeito próprio de falhar. Quem abandona no meio não gravou
 *   nada — e é a home que cobra, com o aviso de cadastro incompleto.
 * - **Pular é resposta.** Todo passo tem saída, inclusive o do consentimento. O que não foi
 *   respondido fica `null`, não é escrito, e volta como aviso. Um passo a passo que prende a pessoa
 *   no segundo item é onde se perde quem ainda não sabe se vai usar o aplicativo.
 * - **Falha na gravação não prende.** A conta existe e o treino não depende disto: a tela avisa,
 *   deixa tentar de novo e deixa seguir.
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
     * As ações de campo são as compartilhadas, com **uma** trocada.
     *
     * O `copy` deixa visível o que só este fluxo faz: aceitar o aviso de saúde acrescenta dois
     * passos à sequência, e recusá-lo os retira. As outras seis são idênticas às da edição de
     * perfil, e por isso não estão escritas aqui.
     */
    val formActions = trainingFormActions(_formState).let { shared ->
        shared.copy(
            onHealthConsentChange = { accepted ->
                shared.onHealthConsentChange(accepted)
                _uiState.update { it.copy(total = OnboardingStep.sequenceFor(accepted).size) }
            },
        )
    }

    /**
     * Se a pergunta dos dias chegou a ser respondida.
     *
     * Existe porque conjunto vazio é ambíguo na gravação: "nenhum dia fixo" é resposta, "pulei" não
     * é. Sem esta marca, pular o passo gravaria um vazio como se fosse escolha.
     */
    private var daysAnswered = false

    /**
     * O passo terminou — pelos dois botões, que fazem quase a mesma coisa.
     *
     * Uma função só porque a diferença entre "Continuar" e "Pular" é exatamente uma: [answered].
     * Ela decide se o passo dos dias conta como respondido e se os campos digitados são conferidos
     * antes de seguir.
     *
     * A validação roda **só nos campos digitados** — peso e altura. Escolha de lista não tem como
     * estar errada. Quem pula não é validado: um campo mal preenchido que a pessoa desistiu de
     * responder não é motivo para prendê-la nele.
     */
    fun onStepDone(answered: Boolean) {
        if (_uiState.value.saving) return

        if (answered && !_formState.updateAndGet { it.validated() }.isValid) return

        advance(answered)
    }

    /**
     * Volta um passo, sem apagar o que já foi respondido.
     *
     * Quem volta quer **corrigir**, não recomeçar: encontrar o campo vazio de novo faria a pessoa
     * digitar duas vezes o que ela só queria conferir.
     */
    fun onBack() {
        val current = _uiState.value
        if (current.saving || !current.canGoBack) return

        val sequence = OnboardingStep.sequenceFor(_formState.value.healthConsent)

        _uiState.update {
            it.copy(step = sequence[current.position - 2], position = current.position - 1, failed = false)
        }
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
