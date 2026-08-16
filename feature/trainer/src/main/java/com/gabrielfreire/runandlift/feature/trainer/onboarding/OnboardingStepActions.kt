package com.gabrielfreire.runandlift.feature.trainer.onboarding

/**
 * Como se anda pelo passo a passo: adiante, adiante sem responder, e para trás.
 *
 * Reunidas para a tela não crescer um parâmetro por botão — ela já recebe o estado, o formulário e
 * as ações dos campos. Separadas de `TrainerFormActions` porque são de naturezas diferentes:
 * aquelas mudam a resposta, estas mudam de tela.
 */
internal data class OnboardingStepActions(val onNext: () -> Unit, val onSkip: () -> Unit, val onBack: () -> Unit)
