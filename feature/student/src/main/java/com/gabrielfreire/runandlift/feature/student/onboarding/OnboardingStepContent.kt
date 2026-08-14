package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * Qual passo desenhar.
 *
 * Um `when` exaustivo sobre [OnboardingStep], em arquivo próprio: acrescentar um passo passa a
 * quebrar a compilação **aqui**, ao lado da lista de passos, em vez de desenhar uma tela em branco
 * em produção.
 */
@Composable
internal fun OnboardingStepContent(
    state: OnboardingUiState,
    form: TrainingFormState,
    actions: TrainingFormActions,
    modifier: Modifier = Modifier,
) {
    when (state.step) {
        OnboardingStep.LEVEL -> LevelStep(
            selected = form.level,
            onSelect = actions.onLevelSelect,
            modifier = modifier,
        )

        OnboardingStep.GOAL -> GoalStep(
            selected = form.goal,
            onSelect = actions.onGoalSelect,
            modifier = modifier,
        )

        OnboardingStep.DAYS -> DaysStep(
            selected = form.availableDays,
            onToggle = actions.onDayToggle,
            modifier = modifier,
        )

        OnboardingStep.HEALTH_CONSENT -> HealthConsentStep(
            accepted = form.healthConsent,
            onChange = actions.onHealthConsentChange,
            modifier = modifier,
        )

        OnboardingStep.MEASURES -> MeasuresStep(
            form = form,
            onWeightChange = actions.onWeightChange,
            onHeightChange = actions.onHeightChange,
            modifier = modifier,
        )

        OnboardingStep.RESTRICTIONS -> RestrictionsStep(
            value = form.restrictions,
            onChange = actions.onRestrictionsChange,
            modifier = modifier,
        )
    }
}

@LightDarkPreviews
@Composable
private fun OnboardingStepContentPreview() {
    RunAndLiftTheme {
        OnboardingStepContent(
            state = OnboardingUiState(step = OnboardingStep.HEALTH_CONSENT, position = 4),
            form = TrainingFormState(),
            actions = previewTrainingFormActions(),
        )
    }
}
