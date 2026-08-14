package com.gabrielfreire.runandlift.feature.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.prefilledFrom
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
 * Edição do perfil de treino — a segunda chance do que o onboarding deixou passar.
 *
 * É a **mesma** tela para completar e para corrigir, e por isso o formulário vem preenchido com o
 * que já existe: quem abre pelo aviso da home encontra os campos vazios que faltavam, e quem abre
 * pelo menu encontra o que respondeu, editável.
 *
 * O que **não** se edita aqui é o que muda a identidade da conta — e-mail e senha. Eles aparecem
 * como leitura (o e-mail) porque a pessoa precisa reconhecer de quem é o perfil; trocá-los exige
 * reautenticação e confirmação, que é outro fluxo.
 *
 * Diferente do onboarding, aqui a gravação **falhar importa**: quem veio corrigir um dado precisa
 * saber se a correção pegou. O estado sai com `failed` e a tela não se fecha.
 */
internal class StudentProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TrainingFormState())
    val formState: StateFlow<TrainingFormState> = _formState.asStateFlow()

    /** Se havia consentimento **antes** desta edição — é o que distingue "aceitar agora" de "já valia". */
    private var consentAlreadyGiven = false

    init {
        viewModelScope.launch { load() }
    }

    fun onLevelSelect(level: TrainingLevel) = _formState.update { it.copy(level = level) }

    fun onGoalSelect(goal: TrainingGoal) = _formState.update { it.copy(goal = goal) }

    fun onDayToggle(day: DayOfWeek) = _formState.update { it.toggleDay(day) }

    fun onWeightChange(value: String) = _formState.update { it.copy(weight = value, weightError = null) }

    fun onHeightChange(value: String) = _formState.update { it.copy(height = value, heightError = null) }

    fun onRestrictionsChange(value: String) = _formState.update { it.copy(restrictions = value) }

    /**
     * Marcar libera peso, altura e restrições; desmarcar os esconde e limpa o que estava digitado.
     *
     * Desmarcar **não apaga** o que já está no banco — retirar consentimento é um pedido de exclusão
     * e merece um fluxo próprio, com confirmação. O que esta tela faz é parar de perguntar.
     */
    fun onHealthConsentChange(accepted: Boolean) {
        _formState.update {
            if (accepted) it.copy(healthConsent = true) else it.copy(healthConsent = false, weight = "", height = "")
        }
    }

    fun onSubmit() {
        if (_uiState.value.saving) return

        val form = _formState.updateAndGet { it.validated() }
        if (!form.isValid) return

        _uiState.update { it.copy(saving = true, failed = false) }
        viewModelScope.launch { save(form) }
    }

    private suspend fun load() {
        val account = authRepository.currentAccountOrNull()
        val uid = account?.uid
        val profile = uid?.let { runCatching { studentRepository.profile(it) }.getOrNull() }
        val name = uid?.let { runCatching { userRepository.profile(it) }.getOrNull() }?.displayName

        consentAlreadyGiven = profile?.hasHealthConsent == true

        _formState.update { it.prefilledFrom(profile) }
        _uiState.update {
            it.copy(
                loading = false,
                name = name.orEmpty(),
                email = account?.email.orEmpty(),
                missing = profile?.let(StudentProfileCompletion::missingIn) ?: MissingStudentData(),
            )
        }
    }

    private suspend fun save(form: TrainingFormState) {
        val uid = authRepository.currentAccountOrNull()?.uid

        val saved = uid != null && runCatching {
            studentRepository.save(
                uid = uid,
                details = form.toDetails(
                    // Na edição os dias sempre vão: a pergunta está na tela, e não há como
                    // "pular" um campo que se está olhando — vazio aqui é escolha.
                    includeDays = true,
                    consentJustGiven = form.healthConsent && !consentAlreadyGiven,
                ),
            )
        }.isSuccess

        _uiState.update { it.copy(saving = false, failed = !saved, saved = saved) }
    }
}
