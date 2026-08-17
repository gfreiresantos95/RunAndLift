package com.gabrielfreire.runandlift.feature.trainer.profile

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
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.onboarding.BioStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.CapacityStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.DaysStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.ExperienceStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.ServiceModesStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.ShowcaseConsentStep
import com.gabrielfreire.runandlift.feature.trainer.onboarding.SpecialtiesStep
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormActions
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.previewTrainerFormActions

/**
 * Todos os campos do perfil profissional, um abaixo do outro.
 *
 * Reusa **os mesmos blocos** do passo a passo, e não cópias deles: `ExperienceStep`,
 * `SpecialtiesStep` e os demais são os passos de lá, aqui empilhados. É o que garante que corrigir
 * uma pergunta no passo a passo corrija também a edição — e é a razão de os blocos não se chamarem
 * `…OnboardingExperience`.
 *
 * O registro no CREF abre a tela como **leitura**, e não some: é a credencial que autoriza tudo o
 * que vem abaixo, e um perfil profissional que não a mostra deixa a dúvida de onde ela foi parar.
 *
 * Apresentação e capacidade só aparecem **com a vitrine aceita**, exatamente como no passo a passo.
 * Quem ainda não aceitou vê a caixa; ao marcar, os campos surgem logo abaixo, no mesmo lugar em que
 * a pergunta foi feita.
 */
@Composable
internal fun ProfessionalFormFields(
    state: TrainerProfileUiState,
    form: TrainerFormState,
    actions: TrainerFormActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXLarge),
    ) {
        AppTextField(
            value = state.cref,
            onValueChange = {},
            label = stringResource(R.string.trainer_field_cref),
            supportingText = stringResource(R.string.trainer_field_cref_support),
            enabled = false,
        )

        FieldGroup(title = stringResource(R.string.trainer_onboarding_experience_title)) {
            ExperienceStep(selected = form.experience, onSelect = actions.onExperienceSelect)
        }

        FieldGroup(title = stringResource(R.string.trainer_onboarding_specialties_title)) {
            SpecialtiesStep(selected = form.specialties, onToggle = actions.onSpecialtyToggle)
        }

        FieldGroup(title = stringResource(R.string.trainer_onboarding_modes_title)) {
            ServiceModesStep(selected = form.serviceModes, onToggle = actions.onServiceModeToggle)
        }

        FieldGroup(title = stringResource(R.string.trainer_onboarding_days_title)) {
            DaysStep(selected = form.availableDays, onToggle = actions.onDayToggle)
        }

        FieldGroup(title = stringResource(R.string.trainer_onboarding_showcase_title)) {
            ShowcaseConsentStep(accepted = form.showcase, onChange = actions.onShowcaseChange)
        }

        if (form.showcase) {
            BioStep(form = form, onBioChange = actions.onBioChange)
            CapacityStep(form = form, onMaxStudentsChange = actions.onMaxStudentsChange)
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
private fun ProfessionalFormFieldsPreview() {
    RunAndLiftTheme {
        ProfessionalFormFields(
            state = TrainerProfileUiState(loading = false, cref = "012345-G/SP"),
            form = TrainerFormState(showcase = false),
            actions = previewTrainerFormActions(),
        )
    }
}
