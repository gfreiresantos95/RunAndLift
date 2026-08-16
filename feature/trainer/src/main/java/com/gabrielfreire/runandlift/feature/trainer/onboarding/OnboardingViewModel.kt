package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.toDetails
import com.gabrielfreire.runandlift.feature.trainer.professionalform.trainerFormActions
import com.gabrielfreire.runandlift.feature.trainer.professionalform.validated
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * O passo a passo que apresenta o treinador aos alunos (backlog E3-02).
 *
 * É o gêmeo do passo a passo do aluno, e vale de propósito: quem tem os dois papéis atravessa o
 * mesmo fluxo duas vezes, com outras perguntas. Abre **logo depois de a conta ser criada**, antes
 * da home, pela mesma razão — deixá-lo para a abertura seguinte é tarde: a pessoa já teria visto a
 * home vazia e formado a impressão de que não há nada a fazer ali.
 *
 * Três decisões governam o fluxo:
 *
 * - **Uma gravação só, no fim.** Os passos mudam estado em memória e não tocam a rede; o documento
 *   é escrito uma vez, ao concluir. Gravar a cada passo custaria sete escritas para responder o que
 *   uma responde, e daria a cada tela um jeito próprio de falhar.
 * - **Pular é resposta.** Todo passo tem saída, inclusive o do consentimento. O que não foi
 *   respondido fica `null`, não é escrito, e volta como aviso na home.
 * - **Falha na gravação não prende.** A conta existe, o registro no CREF já foi gravado no
 *   cadastro, e nada disto impede o treinador de trabalhar: a tela avisa, deixa tentar de novo e
 *   deixa seguir.
 *
 * A diferença que importa em relação ao aluno é o que marca "já aconteceu": lá é a existência do
 * documento, aqui é o carimbo `onboardingDone`. `trainerProfiles/{uid}` já nasce no cadastro com o
 * registro dentro, então a existência dele não diz nada — e quem pulou tudo precisa igualmente
 * nunca mais rever este fluxo.
 */
internal class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val trainerRepository: TrainerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TrainerFormState())
    val formState: StateFlow<TrainerFormState> = _formState.asStateFlow()

    /**
     * As ações de campo são as compartilhadas, com **uma** trocada.
     *
     * O `copy` deixa visível o que só este fluxo faz: aceitar a vitrine acrescenta dois passos à
     * sequência, e recusá-la os retira. As outras seis são idênticas às da edição de perfil, e por
     * isso não estão escritas aqui.
     */
    val formActions = trainerFormActions(_formState).let { shared ->
        shared.copy(
            onShowcaseChange = { accepted ->
                shared.onShowcaseChange(accepted)
                _uiState.update { it.copy(total = OnboardingStep.sequenceFor(accepted).size) }
            },
        )
    }

    /**
     * O passo terminou — pelos dois botões, que fazem quase a mesma coisa.
     *
     * Uma função só porque a diferença entre "Continuar" e "Pular" é exatamente uma: [answered]
     * decide se os campos digitados são conferidos antes de seguir. Quem pula não é validado — um
     * campo mal preenchido que a pessoa desistiu de responder não é motivo para prendê-la nele.
     *
     * A validação roda **só na capacidade**. Escolha de lista não tem como estar errada, e a
     * apresentação já é cortada no limite pelo próprio campo.
     */
    fun onStepDone(answered: Boolean) {
        if (_uiState.value.saving) return

        if (answered && !_formState.updateAndGet { it.validated() }.isValid) return

        advance()
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

        val sequence = OnboardingStep.sequenceFor(_formState.value.showcase)

        _uiState.update {
            it.copy(step = sequence[current.position - 2], position = current.position - 1, failed = false)
        }
    }

    private fun advance() {
        val current = _uiState.value
        val sequence = OnboardingStep.sequenceFor(_formState.value.showcase)

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
     * O aceite da vitrine vai junto **desta** gravação, e é ela que carimba o momento no servidor.
     * A recusa não é enviada: não há nada a desligar num perfil que nunca foi publicado, e mandá-la
     * gravaria um `enabled = false` que só existiria para dizer o que a ausência já diz.
     *
     * Falhar aqui não é motivo para segurar ninguém: o estado sai de `saving` com `failed`, e a
     * tela decide o que oferecer.
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
                trainerRepository.save(
                    uid = uid,
                    details = form
                        .toDetails(answered = false, showcaseChanged = form.showcase)
                        // O carimbo que marca "o passo a passo aconteceu", e o único campo que esta
                        // gravação escreve mesmo quando tudo foi pulado.
                        .copy(onboardingDone = true),
                )
            }.isSuccess

            _uiState.update { it.copy(saving = false, failed = !saved, finished = saved) }
        }
    }
}
