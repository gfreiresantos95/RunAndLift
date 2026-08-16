package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormActions
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.previewTrainerFormActions

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
    form: TrainerFormState,
    actions: TrainerFormActions,
    modifier: Modifier = Modifier,
) {
    when (state.step) {
        OnboardingStep.EXPERIENCE -> ExperienceStep(
            selected = form.experience,
            onSelect = actions.onExperienceSelect,
            modifier = modifier,
        )

        OnboardingStep.SPECIALTIES -> SpecialtiesStep(
            selected = form.specialties,
            onToggle = actions.onSpecialtyToggle,
            modifier = modifier,
        )

        OnboardingStep.SERVICE_MODES -> ServiceModesStep(
            selected = form.serviceModes,
            onToggle = actions.onServiceModeToggle,
            modifier = modifier,
        )

        OnboardingStep.DAYS -> DaysStep(
            selected = form.availableDays,
            onToggle = actions.onDayToggle,
            modifier = modifier,
        )

        OnboardingStep.SHOWCASE_CONSENT -> ShowcaseConsentStep(
            accepted = form.showcase,
            onChange = actions.onShowcaseChange,
            modifier = modifier,
        )

        OnboardingStep.BIO -> BioStep(
            form = form,
            onBioChange = actions.onBioChange,
            modifier = modifier,
        )

        OnboardingStep.CAPACITY -> CapacityStep(
            form = form,
            onMaxStudentsChange = actions.onMaxStudentsChange,
            modifier = modifier,
        )
    }
}

@LightDarkPreviews
@Composable
private fun OnboardingStepContentPreview() {
    RunAndLiftTheme {
        OnboardingStepContent(
            state = OnboardingUiState(step = OnboardingStep.SHOWCASE_CONSENT, position = 5),
            form = TrainerFormState(),
            actions = previewTrainerFormActions(),
        )
    }
}
