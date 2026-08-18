package com.gabrielfreire.runandlift.feature.student.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.link.LinkRequestFailure
import com.gabrielfreire.runandlift.data.link.LinkRequestResult
import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O vínculo visto do lado do aluno: quem treina ele hoje, e o código que cria isso.
 *
 * O caminho tem **dois passos de propósito** — procurar o código e depois confirmar o nome de quem
 * apareceu. Pedir vínculo é autorizar alguém a ler a própria anamnese, e um resgate cego faria isso
 * acontecer no toque seguinte a um erro de digitação. Os dois passos custam a mesma leitura.
 *
 * O nome do aluno é lido junto porque **viaja para dentro do vínculo**: `users/{uid}` é legível só
 * pelo titular, então sem essa cópia a carteira do treinador seria uma lista de identificadores.
 */
internal class MyTrainerViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyTrainerUiState())
    val uiState: StateFlow<MyTrainerUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê os vínculos. Chamado ao abrir e depois de cada mudança. */
    fun refresh() {
        viewModelScope.launch { load() }
    }

    /**
     * O código digitado, em maiúsculo desde a primeira tecla.
     *
     * Subir a caixa aqui e não só na busca é o que faz o campo **parecer** com o código que a pessoa
     * recebeu: ver `abc234` na tela enquanto o papel diz `ABC234` é motivo suficiente para alguém
     * apagar tudo e digitar de novo achando que errou.
     */
    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code.uppercase(), error = null) }
    }

    /** Procura de quem é o código. Não cria nada: o pedido é o passo seguinte. */
    fun onSubmitCode() {
        val state = _uiState.value
        if (!state.canSubmitCode) return

        _uiState.update { it.copy(checking = true, error = null) }

        viewModelScope.launch {
            val invite = runCatching { linkRepository.findInvite(state.code) }

            _uiState.update {
                it.copy(
                    checking = false,
                    invite = invite.getOrNull(),
                    // Código que não existe e leitura que falhou são erros diferentes, e a única
                    // das duas frases que manda conferir a digitação é a primeira.
                    error = when {
                        invite.isFailure -> TrainerCodeError.UNKNOWN
                        invite.getOrNull() == null -> TrainerCodeError.NOT_FOUND
                        else -> null
                    },
                )
            }
        }
    }

    /** Desiste do convite encontrado e volta ao campo, com o código ainda lá para ser corrigido. */
    fun onDismissInvite() {
        _uiState.update { it.copy(invite = null, error = null) }
    }

    /** Confirma o pedido para o treinador que apareceu. */
    fun onConfirmInvite() {
        val state = _uiState.value
        val invite = state.invite ?: return
        if (state.submitting) return

        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch { request(invite) }
    }

    /**
     * Aceitar um convite recebido, ou encerrar o vínculo que existe.
     *
     * Mesma função para as duas pela mesma razão do lado do treinador: o que muda é o estado de
     * destino, e quem autoriza a transição são as Security Rules.
     */
    fun onStatusChange(link: Link, status: LinkStatus) {
        if (_uiState.value.submitting) return

        _uiState.update { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            val changed = runCatching { linkRepository.updateStatus(link, status) }.isSuccess

            load(error = TrainerCodeError.UNKNOWN.takeIf { !changed })
        }
    }

    private suspend fun request(invite: InviteCode) {
        val uid = authRepository.currentAccountOrNull()?.uid
        val name = uid
            ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
            ?.displayName
            .orEmpty()

        val result = uid?.let {
            linkRepository.requestLink(
                invite = invite,
                studentId = it,
                studentName = name,
                // O que a tela já sabe, em vez de uma leitura que a regra nem permitiria: consultar
                // `links/{id}` inexistente volta como permissão negada, e não como "não existe".
                existing = _uiState.value.links.firstOrNull { link -> link.trainerId == invite.trainerId },
            )
        } ?: LinkRequestResult.Failure(LinkRequestFailure.UNKNOWN)

        when (result) {
            is LinkRequestResult.Success -> load()

            is LinkRequestResult.Failure -> _uiState.update {
                it.copy(submitting = false, error = TrainerCodeError.from(result.reason))
            }
        }
    }

    private suspend fun load(error: TrainerCodeError? = null) {
        val uid = authRepository.currentAccountOrNull()?.uid
        val links = uid?.let { runCatching { linkRepository.studentLinks(it) }.getOrNull() }

        _uiState.update {
            it.copy(
                loading = false,
                failed = links == null,
                // A lista antiga sobrevive à falha: quem já via o treinador na tela não o perde
                // porque uma releitura não respondeu.
                links = links ?: it.links,
                // O campo é limpo no sucesso: o código já cumpriu o que tinha de cumprir.
                code = if (links == null) it.code else "",
                checking = false,
                invite = null,
                submitting = false,
                error = error,
            )
        }
    }
}
