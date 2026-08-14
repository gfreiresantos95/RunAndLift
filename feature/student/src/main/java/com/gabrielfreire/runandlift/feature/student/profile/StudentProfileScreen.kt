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
 * **Só o que o treinador precisa ver mora aqui.** Nome, contato e e-mail ficam em "Meus dados", que
 * é outro documento e outro público: aquele só o titular lê, este o treinador vinculado também. Uma
 * tela com os dois juntos esconderia essa diferença de quem precisa dela para decidir o que
 * preencher — e é a diferença que o consentimento de dado de saúde torna concreta.
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

            TrainingFormFields(form = form, actions = actions)

            if (state.failed) {
                Text(
                    text = stringResource(R.string.student_save_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AppButton(
                text = stringResource(R.string.student_action_save),
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
            state = StudentProfileUiState(loading = false, name = "Ana Ribeiro"),
            form = TrainingFormState(healthConsent = true, weight = "72,5", height = "175"),
            actions = previewTrainingFormActions(),
            onSubmit = {},
            onBack = {},
        )
    }
}
