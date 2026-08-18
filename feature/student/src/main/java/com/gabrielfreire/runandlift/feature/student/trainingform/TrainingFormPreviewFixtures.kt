package com.gabrielfreire.runandlift.feature.student.trainingform

// As ações do formulário de treino, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. O que **fica**
// em `TrainingFormActions.kt` é regra de verdade e continua medida: a retirada do consentimento que
// apaga peso, altura e lesões da memória.

/** As dez ações do formulário — a tela se desenha, e nada acontece ao tocar. */
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
