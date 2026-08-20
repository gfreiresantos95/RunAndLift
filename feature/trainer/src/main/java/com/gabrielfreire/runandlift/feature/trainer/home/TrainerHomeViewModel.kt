package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.trainer.profile.MissingTrainerData
import com.gabrielfreire.runandlift.feature.trainer.profile.TrainerProfileCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidade de quem abriu a home do treinador, o que falta no perfil dele e o tamanho da carteira.
 *
 * Lê os três documentos uma vez, na criação, e **não observa**: nem o nome nem o perfil mudam
 * enquanto a tela está aberta, e um `Flow` vivo aqui custaria listeners no Firestore na tela que
 * abre toda vez que o app abre — o oposto da regra 4 do orçamento (§2.4).
 *
 * Por não observar, a home é recarregada ao voltar da edição de perfil: quem corrigiu um dado
 * precisa ver o aviso sumir. É [refresh] que faz isso, e ele é chamado pela navegação, não por um
 * temporizador.
 *
 * **A carteira é o terceiro documento, e ela tem outra regra.** O nome e o perfil só mudam quando o
 * próprio titular mexe; os vínculos mudam quando *outra pessoa* mexe — um aluno digita o código
 * agora e o pedido tem de aparecer na contagem. É por isso que a leitura vai ao servidor
 * (`LinkRepository`, ver ADR-0020) e por isso que releitura ao voltar para a aba importa mais aqui
 * do que no resto da tela. **Custo declarado: 1 leitura por vínculo**, que é a mesma varredura que
 * a aba de alunos já faz — e que `trainerDashboards` substitui quando existir.
 *
 * **Leitura que falha não vira erro de tela**, com uma exceção que não é exceção: a carteira que
 * não respondeu vira `null`, e não zero. A home aparece com a saudação sem nome e sem aviso — e
 * "sem aviso" é a resposta certa: acusar perfil incompleto por causa de uma leitura que não
 * respondeu é um palpite, e treina a pessoa a ignorar avisos. Já dizer "0 alunos" a quem tem trinta
 * não é um palpite, é uma informação errada, e por isso esse caso ganha uma frase própria.
 */
internal class TrainerHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val trainerRepository: TrainerRepository,
    private val linkRepository: LinkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerHomeUiState())
    val uiState: StateFlow<TrainerHomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê o que a tela mostra. Chamado ao abrir e ao voltar para a aba. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            _uiState.value = TrainerHomeUiState(
                loading = false,
                displayName = uid?.let { nameOf(it) },
                missing = uid?.let { TrainerProfileCompletion.missing(trainerRepository, it) }
                    ?: MissingTrainerData(),
                roster = uid?.let { rosterOf(it) },
            )
        }
    }

    private suspend fun nameOf(uid: String): String? = runCatching { userRepository.profile(uid) }
        .getOrNull()
        ?.displayName
        ?.takeIf { it.isNotBlank() }

    /** A carteira contada, ou `null` quando a leitura não respondeu. */
    private suspend fun rosterOf(uid: String): TrainerRoster? = runCatching { linkRepository.trainerLinks(uid) }
        .getOrNull()
        ?.let { TrainerRoster(links = it) }
}
