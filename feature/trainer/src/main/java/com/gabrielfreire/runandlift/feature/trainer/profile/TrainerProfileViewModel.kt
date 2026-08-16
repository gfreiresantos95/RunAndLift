package com.gabrielfreire.runandlift.feature.trainer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.prefilledFrom
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
 * Edição do perfil profissional — a segunda chance do que o passo a passo deixou passar.
 *
 * É a **mesma** tela para completar e para corrigir, e por isso o formulário vem preenchido com o
 * que já existe: quem abre pelo aviso da home encontra os campos vazios que faltavam, e quem abre
 * pelo menu encontra o que respondeu, editável.
 *
 * O que **não** se edita aqui é o registro no CREF: ele aparece como leitura, porque a pessoa
 * precisa reconhecer de quem é o perfil, mas trocá-lo é trocar a habilitação profissional — outro
 * fluxo, com conferência própria.
 *
 * Diferente do passo a passo, aqui a gravação **falhar importa**: quem veio corrigir um dado
 * precisa saber se a correção pegou. O estado sai com `failed` e a tela não se fecha.
 */
internal class TrainerProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val trainerRepository: TrainerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerProfileUiState())
    val uiState: StateFlow<TrainerProfileUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TrainerFormState())
    val formState: StateFlow<TrainerFormState> = _formState.asStateFlow()

    /** Se a vitrine estava aceita **antes** desta edição — é o que distingue mudar de reafirmar. */
    private var showcaseWasEnabled = false

    init {
        viewModelScope.launch { load() }
    }

    /**
     * As ações de campo são as compartilhadas com o passo a passo, sem nenhuma troca.
     *
     * Desmarcar a vitrine esconde apresentação e capacidade e limpa o que estava digitado. O que
     * está no banco **não é apagado** — é o registro do que foi publicado —, mas o perfil sai do ar
     * na gravação: aqui a retirada tem efeito, ao contrário do consentimento de saúde do aluno, em
     * que ela é um pedido de exclusão e merece fluxo próprio.
     */
    val formActions = trainerFormActions(_formState)

    fun onSubmit() {
        if (_uiState.value.saving) return

        val form = _formState.updateAndGet { it.validated() }
        if (!form.isValid) return

        _uiState.update { it.copy(saving = true, failed = false) }
        viewModelScope.launch { save(form) }
    }

    private suspend fun load() {
        val uid = authRepository.currentAccountOrNull()?.uid
        val profile = uid?.let { runCatching { trainerRepository.profile(it) }.getOrNull() }
        val name = uid?.let { runCatching { userRepository.profile(it) }.getOrNull() }?.displayName

        showcaseWasEnabled = profile?.hasShowcaseConsent == true

        _formState.update { it.prefilledFrom(profile) }
        _uiState.update {
            it.copy(
                loading = false,
                name = name.orEmpty(),
                cref = profile?.cref.orEmpty(),
                missing = profile?.let(TrainerProfileCompletion::missingIn) ?: MissingTrainerData(),
            )
        }
    }

    private suspend fun save(form: TrainerFormState) {
        val uid = authRepository.currentAccountOrNull()?.uid

        val saved = uid != null && runCatching {
            trainerRepository.save(
                uid = uid,
                details = form.toDetails(
                    // Na edição as listas sempre vão: a pergunta está na tela, e não há como
                    // "pular" um campo que se está olhando — vazio aqui é escolha.
                    answered = true,
                    // Os dois sentidos contam: aceitar publica, desmarcar tira do ar. Reenviar sem
                    // mudança carimbaria uma data de aceite nova a cada toque em salvar.
                    showcaseChanged = form.showcase != showcaseWasEnabled,
                ),
            )
        }.isSuccess

        _uiState.update { it.copy(saving = false, failed = !saved, saved = saved) }
    }
}
