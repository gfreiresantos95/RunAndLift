package com.gabrielfreire.runandlift.feature.student.onboarding

// A navegação do passo a passo, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura.

/** Adiante, adiante sem responder e para trás — os três sem sair do lugar. */
internal fun previewOnboardingStepActions() = OnboardingStepActions(
    onNext = {},
    onSkip = {},
    onBack = {},
)
