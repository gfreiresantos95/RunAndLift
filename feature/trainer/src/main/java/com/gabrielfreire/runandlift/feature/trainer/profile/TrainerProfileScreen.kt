package com.gabrielfreire.runandlift.feature.trainer.profile

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppMessageCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormActions
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.professionalform.previewTrainerFormActions

/**
 * Editar o perfil profissional — todos os campos numa tela só.
 *
 * É o oposto do passo a passo de propósito: lá, uma pergunta por vez, para não assustar quem acabou
 * de criar a conta; aqui, tudo junto, porque quem abre esta tela sabe o que veio consertar.
 *
 * **Só o que o aluno vê mora aqui.** Nome, contato, e-mail e localidade ficam em "Meus dados", que
 * é outro documento e outro público: aquele só o titular lê, este o aluno vinculado também — e,
 * com a vitrine aceita, qualquer pessoa procurando treinador. Uma tela com os dois juntos
 * esconderia essa diferença de quem precisa dela para decidir o que preencher.
 */
@Composable
internal fun TrainerProfileScreen(
    state: TrainerProfileUiState,
    form: TrainerFormState,
    formActions: TrainerFormActions,
    actions: TrainerProfileActions,
    modifier: Modifier = Modifier,
) {
    // Volta ao salvar, como "Meus dados", e o recibo viaja junto: sair em silêncio faria salvar
    // ficar indistinguível de ter tocado na seta de voltar. Ver `SavedResult`.
    LaunchedEffect(state.saved) {
        if (state.saved) actions.onSaved()
    }

    AppScreenScaffold(
        title = stringResource(R.string.trainer_profile_title),
        modifier = modifier,
        onBack = actions.onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        if (state.loading) {
            AppLoadingState(contentDescription = stringResource(R.string.trainer_loading))
            return@AppScreenScaffold
        }

        ProfessionalFormFields(state = state, form = form, actions = formActions)

        if (state.failed) {
            AppMessageCard(text = stringResource(R.string.trainer_save_failed))
        }

        AppButton(
            text = stringResource(R.string.trainer_action_save),
            onClick = actions.onSubmit,
            loading = state.saving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Perfil do treinador · claro", showBackground = true, heightDp = 1600)
@Preview(
    name = "Perfil do treinador · escuro",
    showBackground = true,
    heightDp = 1600,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TrainerProfileScreenPreview() {
    RunAndLiftTheme {
        TrainerProfileScreen(
            state = TrainerProfileUiState(loading = false, name = "Carlos Pereira", cref = "012345-G/SP"),
            form = TrainerFormState(showcase = true, maxStudents = "20"),
            formActions = previewTrainerFormActions(),
            actions = TrainerProfileActions(onSubmit = {}, onSaved = {}, onBack = {}),
        )
    }
}
