package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek

/**
 * O que a tela do formulário de treino pode fazer.
 *
 * Existe pelo mesmo motivo do `ProfileFormActions` do `:feature:auth`: sete callbacks passados um a
 * um estourariam o limite de parâmetros da função, e cada tela nova que reunisse o formulário teria
 * de repetir a lista inteira. Reunidos, o que muda é o conteúdo desta classe.
 *
 * As duas telas que a usam — o onboarding e a edição de perfil — implementam **as mesmas** ações,
 * porque os campos são os mesmos; o que difere é quando cada uma grava.
 */
internal data class TrainingFormActions(
    val onLevelSelect: (TrainingLevel) -> Unit,
    val onGoalSelect: (TrainingGoal) -> Unit,
    val onDayToggle: (DayOfWeek) -> Unit,
    val onWeightChange: (String) -> Unit,
    val onHeightChange: (String) -> Unit,
    val onInjuryToggle: (InjuryArea) -> Unit,
    val onNoInjuriesToggle: () -> Unit,
    val onOtherInjuryToggle: () -> Unit,
    val onInjuryNotesChange: (String) -> Unit,
    val onHealthConsentChange: (Boolean) -> Unit,
)

/**
 * As ações que **todo** dono deste formulário implementa da mesma forma: escrever o campo no estado
 * e limpar o erro dele.
 *
 * Existe porque o onboarding e a edição de perfil tinham todas idênticas, uma a uma, e a única que
 * difere de verdade — o consentimento — some no meio das outras quando estão todas escritas à mão.
 * Aqui as triviais somem, e quem precisa mudar aquela o faz com um `copy`, que deixa a diferença
 * visível.
 *
 * As três de lesão delegam a [TrainingFormState], e não decidem nada: a exclusividade entre
 * "Nenhuma" e as regiões é regra do formulário, e uma tela nova não pode ter a chance de esquecê-la.
 *
 * Retirar o consentimento **limpa peso, altura e lesões**: dado sensível não fica em memória à
 * espera de uma autorização que foi retirada. Isso vale para as duas telas, então mora aqui.
 */
internal fun trainingFormActions(state: MutableStateFlow<TrainingFormState>) = TrainingFormActions(
    onLevelSelect = { level -> state.update { it.copy(level = level) } },
    onGoalSelect = { goal -> state.update { it.copy(goal = goal) } },
    onDayToggle = { day -> state.update { it.toggleDay(day) } },
    onWeightChange = { value -> state.update { it.copy(weight = value, weightError = null) } },
    onHeightChange = { value -> state.update { it.copy(height = value, heightError = null) } },
    onInjuryToggle = { area -> state.update { it.toggleInjury(area) } },
    onNoInjuriesToggle = { state.update { it.toggleNoInjuries() } },
    onOtherInjuryToggle = { state.update { it.toggleOtherInjury() } },
    onInjuryNotesChange = { value -> state.update { it.copy(injuryNotes = value) } },
    onHealthConsentChange = { accepted -> state.update { it.withHealthConsent(accepted) } },
)

/**
 * O aceite entrando ou saindo.
 *
 * Sair **apaga o que já foi respondido de saúde**, e não só esconde: uma autorização retirada com o
 * peso ainda em memória é a autorização valendo na prática. O que fica é o que não é dado de saúde —
 * nível, objetivo e dias.
 */
private fun TrainingFormState.withHealthConsent(accepted: Boolean): TrainingFormState = if (accepted) {
    copy(healthConsent = true)
} else {
    copy(
        healthConsent = false,
        weight = "",
        height = "",
        injuries = emptySet(),
        noInjuries = false,
        otherInjury = false,
        injuryNotes = "",
    )
}

/** As ações sem efeito, para os previews — a tela se desenha, e nada acontece ao tocar. */
internal fun previewTrainingFormActions() = TrainingFormActions(
    onLevelSelect = {},
    onGoalSelect = {},
    onDayToggle = {},
    onWeightChange = {},
    onHeightChange = {},
    onInjuryToggle = {},
    onNoInjuriesToggle = {},
    onOtherInjuryToggle = {},
    onInjuryNotesChange = {},
    onHealthConsentChange = {},
)
