package com.gabrielfreire.runandlift.feature.trainer.programeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.program.ProgramRepository
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Carrega, guarda e grava o programa que está sendo montado.
 *
 * **Vive na entrada do editor na pilha, e é compartilhado por três telas** — o programa, o dia e a
 * prescrição. É o que permite montar um treino inteiro sem tocar a rede: o rascunho fica em memória
 * enquanto se anda entre as telas, e só há escrita quando alguém toca em salvar. A alternativa —
 * cada tela lendo e gravando o próprio pedaço — custaria uma leitura por dia aberto e uma escrita
 * por número ajustado, que é o oposto do orçamento (§2.4).
 *
 * As mutações não estão aqui, estão em [ProgramDraftController]. O que sobra é o que só um ViewModel
 * pode fazer: ler o documento, gravar, e dizer se deu certo.
 *
 * @param programId o id vindo da rota, ou [TrainerRoutes.NEW_PROGRAM] para um programa que ainda não
 *   existe — que nasce em memória e não custa leitura nenhuma.
 */
internal class ProgramEditorViewModel(
    private val authRepository: AuthRepository,
    private val programRepository: ProgramRepository,
    private val exerciseRepository: ExerciseRepository,
    private val programId: String,
) : ViewModel() {

    /** O rascunho e tudo o que mexe nele. A tela chama os métodos daqui direto. */
    val draft = ProgramDraftController()

    private val status = MutableStateFlow(
        ProgramEditorUiState(loading = programId != TrainerRoutes.NEW_PROGRAM),
    )

    /**
     * O estado da tela: o rascunho vivo mais a situação da leitura e da gravação.
     *
     * São duas fontes combinadas porque mudam por motivos diferentes e em ritmos diferentes — o
     * rascunho a cada tecla digitada, a situação uma vez por operação. Juntá-las num
     * `MutableStateFlow` só faria toda mutação ter de copiar o estado inteiro.
     */
    val uiState: StateFlow<ProgramEditorUiState> = combine(status, draft.draft) { current, program ->
        current.copy(program = program)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = status.value,
    )

    init {
        load()
    }

    /**
     * Lê o programa, se ele já existir.
     *
     * Falha e ausência caem no mesmo lugar — [ProgramEditorUiState.notFound] —, e isso é uma escolha:
     * a tela é um editor, e abrir um formulário em branco no lugar de um programa que não pôde ser
     * lido faria o treinador remontá-lo por cima do que continua gravado. Melhor dizer que não deu.
     */
    private fun load() {
        if (programId == TrainerRoutes.NEW_PROGRAM) return

        viewModelScope.launch {
            val program = runCatching { programRepository.program(programId) }.getOrNull()

            if (program == null) {
                status.update { it.copy(loading = false, notFound = true) }
            } else {
                draft.current = program
                status.update { it.copy(loading = false) }
            }
        }
    }

    /**
     * O exercício escolhido no catálogo virando prescrição, no dia que o pediu.
     *
     * O catálogo devolve só o id — ele não sabe, nem precisa saber, em que dia o exercício entra. É
     * aqui que o id vira um [com.gabrielfreire.runandlift.data.model.Exercise], e a busca sai do
     * **Room**: custo zero de leitura do Firestore, e funciona sem sinal.
     *
     * Id que não está no catálogo local não faz nada. Acontece se o catálogo for republicado entre
     * a escolha e a volta — raro, e a resposta certa é não acrescentar um exercício fantasma.
     */
    fun addExerciseFromCatalog(dayIndex: Int, exerciseId: String) {
        viewModelScope.launch {
            val exercise = exerciseRepository.observeById(exerciseId).first() ?: return@launch

            draft.onAddExercise(dayIndex = dayIndex, exercise = exercise)
        }
    }

    /**
     * Grava e avisa quem chamou que deu certo.
     *
     * O rascunho é atualizado com o programa como ele ficou — inclusive o id, que um programa novo
     * só ganha na escrita. Isso importa mesmo saindo da tela em seguida: sem o id em memória, um
     * segundo toque em salvar antes de a navegação acontecer criaria um segundo documento.
     *
     * **Sem rede, falha, e a tela não fecha.** [onSaved] só é chamado quando a escrita voltou. A
     * fila durável (E0-04) chega junto do registro de série; aqui o comportamento certo é dizer que
     * não gravou, e não desempilhar fingindo que sim.
     */
    fun save(onSaved: () -> Unit) {
        val current = uiState.value
        if (!current.canSave) return

        viewModelScope.launch {
            status.update { it.copy(saving = true, saveFailed = false) }

            val uid = authRepository.currentAccountOrNull()?.uid
            if (uid == null) {
                status.update { it.copy(saving = false, saveFailed = true) }
                return@launch
            }

            runCatching { programRepository.save(current.program.copy(trainerId = uid)) }
                .onSuccess { saved ->
                    draft.current = saved
                    status.update { it.copy(saving = false) }
                    onSaved()
                }
                .onFailure { status.update { it.copy(saving = false, saveFailed = true) } }
        }
    }

    private companion object {
        /**
         * Quanto o estado combinado sobrevive sem ninguém olhando.
         *
         * Cinco segundos é o valor que atravessa uma rotação de tela sem recomeçar. Zero faria o
         * `combine` reiniciar a cada giro, e o rascunho piscaria de volta ao valor inicial.
         */
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
