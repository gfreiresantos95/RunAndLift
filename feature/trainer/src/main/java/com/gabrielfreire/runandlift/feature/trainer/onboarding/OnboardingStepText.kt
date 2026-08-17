package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O título e a linha de apoio de cada passo.
 *
 * Extensões de [OnboardingStep], no arquivo do tipo que elas estendem — a regra do projeto para
 * enum e suas mensagens. Acrescentar um passo quebra os dois `when` abaixo, e não uma tela em
 * branco em produção.
 *
 * Os títulos são **perguntas em segunda pessoa**, porque é uma conversa: "O que você atende?" e não
 * "Especialidades". A linha de apoio diz sempre para que a resposta serve — aqui, quase sempre, que
 * é o aluno do outro lado que vai lê-la.
 */
@Composable
internal fun OnboardingStep.title(): String = stringResource(
    when (this) {
        OnboardingStep.EXPERIENCE -> R.string.trainer_onboarding_experience_title
        OnboardingStep.SPECIALTIES -> R.string.trainer_onboarding_specialties_title
        OnboardingStep.SERVICE_MODES -> R.string.trainer_onboarding_modes_title
        OnboardingStep.DAYS -> R.string.trainer_onboarding_days_title
        OnboardingStep.SHOWCASE_CONSENT -> R.string.trainer_onboarding_showcase_title
        OnboardingStep.BIO -> R.string.trainer_onboarding_bio_title
        OnboardingStep.CAPACITY -> R.string.trainer_onboarding_capacity_title
    },
)

@Composable
internal fun OnboardingStep.subtitle(): String = stringResource(
    when (this) {
        OnboardingStep.EXPERIENCE -> R.string.trainer_onboarding_experience_subtitle
        OnboardingStep.SPECIALTIES -> R.string.trainer_onboarding_specialties_subtitle
        OnboardingStep.SERVICE_MODES -> R.string.trainer_onboarding_modes_subtitle
        OnboardingStep.DAYS -> R.string.trainer_onboarding_days_subtitle
        OnboardingStep.SHOWCASE_CONSENT -> R.string.trainer_onboarding_showcase_subtitle
        OnboardingStep.BIO -> R.string.trainer_onboarding_bio_subtitle
        OnboardingStep.CAPACITY -> R.string.trainer_onboarding_capacity_subtitle
    },
)
