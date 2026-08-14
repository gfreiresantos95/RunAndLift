package com.gabrielfreire.runandlift.feature.student.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.onboarding.DaysStep
import com.gabrielfreire.runandlift.feature.student.onboarding.GoalStep
import com.gabrielfreire.runandlift.feature.student.onboarding.HealthConsentStep
import com.gabrielfreire.runandlift.feature.student.onboarding.InjuriesStep
import com.gabrielfreire.runandlift.feature.student.onboarding.LevelStep
import com.gabrielfreire.runandlift.feature.student.onboarding.MeasuresStep
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * Todos os campos do perfil de treino, um abaixo do outro.
 *
 * Reusa **os mesmos blocos** do onboarding, e não cópias deles: `LevelStep`, `GoalStep` e os demais
 * são os passos de lá, aqui empilhados. É o que garante que corrigir uma pergunta no onboarding
 * corrija também a edição — e é a razão de os blocos não se chamarem `…OnboardingLevel`.
 *
 * Peso, altura e restrições só aparecem **com consentimento**, exatamente como no onboarding. Quem
 * ainda não aceitou vê a caixa; ao marcar, os campos surgem logo abaixo, no mesmo lugar em que a
 * pergunta foi feita.
 */
@Composable
internal fun TrainingFormFields(form: TrainingFormState, actions: TrainingFormActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXLarge),
    ) {
        FieldGroup(title = stringResource(R.string.student_onboarding_level_title)) {
            LevelStep(selected = form.level, onSelect = actions.onLevelSelect)
        }

        FieldGroup(title = stringResource(R.string.student_onboarding_goal_title)) {
            GoalStep(selected = form.goal, onSelect = actions.onGoalSelect)
        }

        FieldGroup(title = stringResource(R.string.student_onboarding_days_title)) {
            DaysStep(selected = form.availableDays, onToggle = actions.onDayToggle)
        }

        FieldGroup(title = stringResource(R.string.student_onboarding_health_title)) {
            HealthConsentStep(accepted = form.healthConsent, onChange = actions.onHealthConsentChange)
        }

        if (form.healthConsent) {
            MeasuresStep(
                form = form,
                onWeightChange = actions.onWeightChange,
                onHeightChange = actions.onHeightChange,
            )

            // Com título próprio, ao contrário de peso e altura: a lista de regiões não se explica
            // sozinha do jeito que dois campos rotulados se explicam.
            FieldGroup(title = stringResource(R.string.student_onboarding_injuries_title)) {
                InjuriesStep(form = form, actions = actions)
            }
        }
    }
}

@Composable
private fun FieldGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@LightDarkPreviews
@Composable
private fun TrainingFormFieldsPreview() {
    RunAndLiftTheme {
        TrainingFormFields(
            form = TrainingFormState(healthConsent = false),
            actions = previewTrainingFormActions(),
        )
    }
}
