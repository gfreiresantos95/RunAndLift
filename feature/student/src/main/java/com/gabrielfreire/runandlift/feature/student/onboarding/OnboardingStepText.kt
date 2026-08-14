package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O título e a linha de apoio de cada passo.
 *
 * Extensões de [OnboardingStep], no arquivo do tipo que elas estendem — a regra do projeto para
 * enum e suas mensagens. Acrescentar um passo quebra os dois `when` abaixo, e não uma tela em
 * branco em produção.
 *
 * Os títulos são **perguntas em segunda pessoa**, porque é uma conversa: "Qual é o seu objetivo?" e
 * não "Objetivo". A linha de apoio diz sempre para que a resposta serve — é o que faz a pessoa
 * responder em vez de pular.
 */
@Composable
internal fun OnboardingStep.title(): String = stringResource(
    when (this) {
        OnboardingStep.LEVEL -> R.string.student_onboarding_level_title
        OnboardingStep.GOAL -> R.string.student_onboarding_goal_title
        OnboardingStep.DAYS -> R.string.student_onboarding_days_title
        OnboardingStep.HEALTH_CONSENT -> R.string.student_onboarding_health_title
        OnboardingStep.MEASURES -> R.string.student_onboarding_measures_title
        OnboardingStep.RESTRICTIONS -> R.string.student_onboarding_restrictions_title
    },
)

@Composable
internal fun OnboardingStep.subtitle(): String = stringResource(
    when (this) {
        OnboardingStep.LEVEL -> R.string.student_onboarding_level_subtitle
        OnboardingStep.GOAL -> R.string.student_onboarding_goal_subtitle
        OnboardingStep.DAYS -> R.string.student_onboarding_days_subtitle
        OnboardingStep.HEALTH_CONSENT -> R.string.student_onboarding_health_subtitle
        OnboardingStep.MEASURES -> R.string.student_onboarding_measures_subtitle
        OnboardingStep.RESTRICTIONS -> R.string.student_onboarding_restrictions_subtitle
    },
)
