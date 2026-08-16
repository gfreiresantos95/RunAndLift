package com.gabrielfreire.runandlift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.completeprofile.ProfileCompletion
import com.gabrielfreire.runandlift.feature.auth.navigation.AuthRoutes
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerRoutes
import com.gabrielfreire.runandlift.navigation.RoleRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de abertura: decide para onde o app vai antes de a splash sair.
 *
 * Quatro desfechos possíveis, e a ordem importa:
 * 1. sem sessão -> fluxo de entrada;
 * 2. com sessão e sem papel -> escolha de papel, porque a conta existe mas não sabe o que é;
 * 3. com sessão, com papel e cadastro pela metade -> conclusão de cadastro;
 * 4. com sessão e cadastro completo -> direto para o grafo do papel.
 *
 * O terceiro caso é o que impede que **fechar o app** vire a forma de pular o que a conclusão de
 * cadastro pergunta. Sem ele, quem entra pelo Google e mata o aplicativo na tela de conclusão volta
 * direto para a home — sem data de nascimento, sem aceite dos termos e, se for treinador, sem o
 * registro que a lei exige de quem prescreve.
 *
 * Resolver isso aqui, e não depois da primeira composição, é o que evita o app abrir na tela de
 * login e trocar para a home um frame depois — o piscar que o backlog quer evitar na abertura.
 */
class MainViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val trainerRepository: TrainerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val account = authRepository.currentAccountOrNull()
            // Perfil vem do cache do Firestore quando existe, então abrir offline com sessão
            // ativa continua funcionando.
            val profile = account?.let { runCatching { userRepository.profile(it.uid) }.getOrNull() }
            val role = profile?.activeRole

            // Custa 0 leitura com o cache quente, que é o caso de toda abertura depois da
            // primeira. Leitura que falha responde "está completo", então rede ruim nunca segura
            // ninguém na porta.
            val incomplete = account != null && role != null &&
                ProfileCompletion.missing(userRepository, account.uid, role).any

            val onboarding = if (account != null && role != null && !incomplete) {
                onboardingRouteFor(account.uid, role)
            } else {
                null
            }

            _uiState.value = MainUiState(
                ready = true,
                startDestination = startDestinationFor(account != null, role, incomplete, onboarding),
                activeRole = role,
                canSwitchRole = profile?.roles?.hasBoth == true,
            )
        }
    }

    /**
     * Para onde ir **assim que a conta é autenticada**, e não só na abertura seguinte.
     *
     * Existe porque quem acabou de criar conta precisa responder o passo a passo do seu papel
     * **antes** de ver a home — do contrário ele só apareceria no próximo lançamento do app, que é
     * tarde demais: a essa altura a pessoa já viu a home vazia e formou a impressão de que não há
     * nada a fazer ali.
     *
     * A pergunta é a mesma da abertura, e a resposta vem da mesma fonte: [onboardingRouteFor].
     *
     * Custo declarado: **0 leitura** com o documento em cache — que é o caso logo depois do
     * cadastro, porque a gravação do perfil acabou de passar por ali.
     */
    fun destinationAfterAuth(role: ActiveRole, onResolved: (String) -> Unit) {
        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid

            onResolved(uid?.let { onboardingRouteFor(it, role) } ?: RoleRoutes.graphFor(role))
        }
    }

    /**
     * A rota do passo a passo do papel, ou `null` quando ele já aconteceu.
     *
     * **Os dois papéis têm passo a passo, e a marca de "já aconteceu" é diferente em cada um** —
     * porque os documentos nascem em momentos diferentes:
     *
     * - **Aluno**: documento inexistente em `students/{uid}`. Nada o cria antes do fim do fluxo,
     *   então a existência dele é a marca — inclusive para quem pulou tudo, que já tem documento e
     *   não deve rever o passo a passo.
     * - **Treinador**: o carimbo `onboarded` em `trainerProfiles/{uid}`. Ali a existência não diz
     *   nada: o cadastro abre o documento com o registro no CREF dentro, e usar "existe" mandaria
     *   todo treinador direto para a home sem nunca ter respondido nada.
     *
     * Leitura que falha responde "já aconteceu", pela mesma razão de `ProfileCompletion`: sem rede
     * e sem cache, repetir um passo a passo é pior do que deixá-lo passar.
     *
     * Custo declarado: **0 leitura** com o documento em cache, 1 quando não está.
     */
    private suspend fun onboardingRouteFor(uid: String, role: ActiveRole): String? = when (role) {
        ActiveRole.STUDENT -> StudentRoutes.ONBOARDING.takeIf {
            runCatching { studentRepository.profile(uid) }.map { it == null }.getOrDefault(false)
        }

        ActiveRole.TRAINER -> TrainerRoutes.ONBOARDING.takeIf {
            runCatching { trainerRepository.profile(uid) }.map { it?.onboarded != true }.getOrDefault(false)
        }
    }

    /** Troca o papel ativo e devolve o novo, ou `null` se a troca não se aplica. */
    fun switchRole(onSwitched: (ActiveRole) -> Unit) {
        val current = _uiState.value
        val target = when (current.activeRole) {
            ActiveRole.TRAINER -> ActiveRole.STUDENT
            ActiveRole.STUDENT -> ActiveRole.TRAINER
            null -> return
        }
        if (!current.canSwitchRole) return

        viewModelScope.launch {
            val uid = authRepository.currentAccountOrNull()?.uid ?: return@launch
            runCatching { userRepository.setActiveRole(uid, target) }
                .onSuccess {
                    _uiState.value = current.copy(activeRole = target)
                    onSwitched(target)
                }
        }
    }

    private fun startDestinationFor(
        hasAccount: Boolean,
        role: ActiveRole?,
        incomplete: Boolean,
        onboarding: String?,
    ): String = when {
        !hasAccount -> AuthRoutes.GRAPH

        role == null -> AuthRoutes.ROLE_SELECTION

        incomplete -> AuthRoutes.completeProfile(role)

        // Depois do cadastro completo: o passo a passo do papel — quem o aluno é para o treinador,
        // ou quem o treinador é para o aluno. Vem **depois** da conclusão de cadastro porque aquela
        // é obrigação legal e esta é conversa de produto; inverter a ordem pediria dado de saúde
        // antes do aceite dos termos, e apresentação profissional antes do registro no CREF.
        onboarding != null -> onboarding

        else -> RoleRoutes.graphFor(role)
    }
}
