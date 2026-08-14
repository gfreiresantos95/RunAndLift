package com.gabrielfreire.runandlift.feature.student.profile

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.core.designsystem.component.AppTopBar
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.trainingform.previewTrainingFormActions

/**
 * Editar o perfil de treino — todos os campos numa tela só.
 *
 * É o oposto do onboarding de propósito: lá, uma pergunta por vez, para não assustar quem acabou de
 * criar a conta; aqui, tudo junto, porque quem abre esta tela sabe o que veio consertar.
 *
 * O e-mail aparece **desabilitado**, e não escondido: quem edita o perfil precisa reconhecer de
 * qual conta ele é, e um campo ausente levanta a dúvida de onde se troca aquilo. Desabilitado com
 * a explicação embaixo responde a pergunta antes de ela ser feita.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudentProfileScreen(
    state: StudentProfileUiState,
    form: TrainingFormState,
    actions: TrainingFormActions,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.student_profile_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.student_action_back),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .padding(paddingValues = Dimens.ScreenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
        ) {
            if (state.loading) return@Column

            AppTextField(
                value = state.email,
                onValueChange = {},
                label = stringResource(R.string.student_field_email),
                supportingText = stringResource(R.string.student_field_email_support),
                enabled = false,
            )

            TrainingFormFields(form = form, actions = actions)

            if (state.failed) {
                Text(
                    text = stringResource(R.string.student_profile_save_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppButton(
                text = stringResource(R.string.student_profile_save),
                onClick = onSubmit,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Perfil do aluno · claro", showBackground = true, heightDp = 1400)
@Preview(
    name = "Perfil do aluno · escuro",
    showBackground = true,
    heightDp = 1400,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentProfileScreenPreview() {
    RunAndLiftTheme {
        StudentProfileScreen(
            state = StudentProfileUiState(loading = false, name = "Ana Ribeiro", email = "ana@exemplo.com"),
            form = TrainingFormState(healthConsent = true, weight = "72,5", height = "175"),
            actions = previewTrainingFormActions(),
            onSubmit = {},
            onBack = {},
        )
    }
}
