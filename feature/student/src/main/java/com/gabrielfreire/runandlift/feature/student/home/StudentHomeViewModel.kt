package com.gabrielfreire.runandlift.feature.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.student.profile.MissingStudentData
import com.gabrielfreire.runandlift.feature.student.profile.StudentProfileCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identidade de quem abriu a home, o que falta no perfil dele, e quem o treina.
 *
 * Lê na criação e **não observa**: nem o nome, nem o perfil de treino, nem o vínculo mudam enquanto
 * a tela está aberta, e um `Flow` vivo aqui custaria listeners no Firestore na tela que abre toda
 * vez que o app abre — o oposto da regra 4 do orçamento (§2.4).
 *
 * Por não observar, a home é recarregada ao voltar da edição de perfil: quem corrigiu um dado
 * precisa ver o aviso sumir. É [refresh] que faz isso, e ele é chamado pela navegação, não por um
 * temporizador.
 *
 * **Leitura que falha não vira erro de tela.** Sem rede e sem cache, a home aparece com a saudação
 * sem nome, sem aviso e sem treinador — e as três ausências são a resposta certa: acusar cadastro
 * incompleto, ou nomear um treinador, por causa de uma leitura que não respondeu é um palpite.
 */
internal class StudentHomeViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val linkRepository: LinkRepository,
    private val trainerRepository: TrainerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Relê o que a tela mostra. Chamado ao abrir e ao voltar da edição de perfil. */
    fun refresh() {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            val name = uid
                ?.let { runCatching { userRepository.profile(it) }.getOrNull() }
                ?.displayName
                ?.takeIf { it.isNotBlank() }

            val missing = uid
                ?.let { StudentProfileCompletion.missing(studentRepository, it) }
                ?: MissingStudentData()

            _uiState.value = StudentHomeUiState(
                loading = false,
                displayName = name,
                missing = missing,
                trainer = uid?.let { linkedTrainer(it) },
            )
        }
    }

    /**
     * O treinador que acompanha esta pessoa **agora**, montado a partir de duas leituras.
     *
     * A primeira é a lista de vínculos, que custa 1 leitura por documento — na prática um ou dois,
     * porque um aluno tem um treinador de cada vez e no máximo alguns encerrados. A segunda é o
     * perfil profissional, que custa 1 leitura e traz o registro no CREF; ela só acontece quando há
     * vínculo vigente, então quem ainda não se vinculou não paga por ela.
     *
     * O registro que não vem **não cancela o treinador**: o nome sozinho já responde à pergunta
     * principal da linha, e esconder a pessoa inteira porque um segundo documento não chegou seria
     * trocar uma informação parcial por nenhuma.
     */
    private suspend fun linkedTrainer(uid: String): LinkedTrainer? {
        val link = runCatching { linkRepository.studentLinks(uid) }.getOrNull()?.current() ?: return null

        val cref = runCatching { trainerRepository.profile(link.trainerId) }.getOrNull()?.cref

        return LinkedTrainer(
            name = link.trainerName,
            cref = cref,
            since = LinkedTrainer.dateOf(link.createdAt),
        )
    }

    /**
     * O vínculo que faz alguém ser "quem me acompanha": ativo, ou pausado.
     *
     * **Pedido e convite não entram**, ao contrário da tela "Meu treinador". Lá a pergunta é qual
     * vínculo está em jogo, e um pedido pendente é a resposta; aqui a linha afirma que aquela pessoa
     * acompanha este aluno, e afirmar isso de quem ainda não aceitou seria dizer algo que não é
     * verdade. Encerrado também fica de fora, pelo motivo óbvio.
     *
     * Pausado entra porque a relação existe — está suspensa, não desfeita —, e ativo ganha o empate
     * improvável de dois vigentes.
     */
    private fun List<Link>.current(): Link? = firstOrNull { it.status == LinkStatus.ACTIVE }
        ?: firstOrNull { it.status == LinkStatus.PAUSED }
}
