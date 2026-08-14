package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
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
    val onRestrictionsChange: (String) -> Unit,
    val onHealthConsentChange: (Boolean) -> Unit,
)

/** As ações sem efeito, para os previews — a tela se desenha, e nada acontece ao tocar. */
internal fun previewTrainingFormActions() = TrainingFormActions(
    onLevelSelect = {},
    onGoalSelect = {},
    onDayToggle = {},
    onWeightChange = {},
    onHeightChange = {},
    onRestrictionsChange = {},
    onHealthConsentChange = {},
)
