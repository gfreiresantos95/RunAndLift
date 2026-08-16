package com.gabrielfreire.runandlift.feature.trainer.onboarding

// As ações de navegação do passo a passo, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Isto existe
// para desenhar tela no Android Studio e não roda em produção.

/** Andar para frente, pular e voltar — os três sem fazer nada. */
internal fun previewOnboardingStepActions() = OnboardingStepActions(
    onNext = {},
    onSkip = {},
    onBack = {},
)
