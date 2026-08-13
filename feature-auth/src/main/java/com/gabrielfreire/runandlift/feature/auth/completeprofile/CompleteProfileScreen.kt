package com.gabrielfreire.runandlift.feature.auth.completeprofile
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.AuthHeadline
import com.gabrielfreire.runandlift.feature.auth.component.AuthScreenLayout
import com.gabrielfreire.runandlift.feature.auth.component.FailureBanner
import com.gabrielfreire.runandlift.feature.auth.component.RoleChip
import com.gabrielfreire.runandlift.feature.auth.profileform.ConsentFields
import com.gabrielfreire.runandlift.feature.auth.profileform.ContactFields
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormActions
import com.gabrielfreire.runandlift.feature.auth.profileform.ProfileFormState
import com.gabrielfreire.runandlift.feature.auth.profileform.TrainerFields
import com.gabrielfreire.runandlift.feature.auth.profileform.previewProfileFormActions

/**
 * Concluir cadastro — a tela que existe porque o Google não pergunta.
 *
 * Quem chega aqui **já está autenticado**: a conta existe, e o que falta é o que nenhum provedor de
 * identidade tem para dar. Por isso ela não tem seta de voltar nem saída alternativa: voltar levaria
 * a um login que já aconteceu, e sair pela metade produziria a conta incompleta que ela existe para
 * impedir. É também por isso que o app volta para cá se for fechado no meio — a pergunta não se
 * evita fechando o aplicativo.
 *
 * **Pede só o que falta.** O que já existe volta preenchido, e o consentimento some para quem já
 * consentiu. O nome aparece como confirmação, não como campo: a pessoa acabou de escolher uma conta
 * numa folha do sistema, e dizer qual foi é mais barato que deixá-la em dúvida.
 *
 * A ordem é a mesma do cadastro por formulário — o que o produto precisa, depois o que a lei exige
 * — porque é a mesma conversa, retomada de onde o provedor parou. Os blocos também são os mesmos,
 * e não cópias: [ContactFields], [TrainerFields] e [ConsentFields] são exatamente os
 * do cadastro.
 */
@Composable
internal fun CompleteProfileScreen(
    state: CompleteProfileUiState,
    form: ProfileFormState,
    actions: ProfileFormActions,
    onSubmit: () -> Unit,
    onCompleted: (ActiveRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.completedRole) {
        state.completedRole?.let(block = onCompleted)
    }

    AuthScreenLayout(modifier = modifier, bottom = {}) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            RoleChip(role = state.role)
        }

        AuthHeadline(
            title = stringResource(id = R.string.auth_complete_title),
            subtitle = state.name.takeIf { it.isNotBlank() }
                ?.let { stringResource(id = R.string.auth_complete_subtitle_named, it) }
                ?: stringResource(id = R.string.auth_complete_subtitle),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        // Enquanto se descobre o que falta, nada é desenhado: campos que aparecem preenchidos um
        // instante depois dão a impressão de que a tela se corrigiu sozinha.
        if (state.loading) return@AuthScreenLayout

        val enabled = !state.submitting

        ContactFields(form = form, formActions = actions, role = state.role, enabled = enabled)

        if (state.role == ActiveRole.TRAINER) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))
            TrainerFields(
                cref = form.cref,
                onCrefChange = actions.onCrefChange,
                crefError = form.crefError,
                enabled = enabled,
            )
        }

        if (state.askConsent) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            ConsentFields(form = form, formActions = actions, enabled = enabled)
        }

        if (state.failed) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMedium))
            // `UNKNOWN` e não `NO_NETWORK`: rede é a causa provável, não a causa sabida, e
            // mandar alguém conferir a internet que está funcionando é pior que não palpitar.
            FailureBanner(failure = AuthFailure.UNKNOWN)
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        AppButton(
            text = stringResource(id = R.string.auth_complete_action),
            onClick = onSubmit,
            loading = state.submitting,
        )
    }
}

@Preview(name = "Concluir cadastro · treinador, claro", showBackground = true, heightDp = 1200)
@Preview(
    name = "Concluir cadastro · treinador, escuro",
    showBackground = true,
    heightDp = 1200,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CompleteProfilePreview() {
    RunAndLiftTheme {
        CompleteProfileScreen(
            state = CompleteProfileUiState(role = ActiveRole.TRAINER, loading = false, name = "Bruno Lima"),
            form = ProfileFormState(),
            actions = previewProfileFormActions(),
            onSubmit = {},
            onCompleted = {},
        )
    }
}

/** Aluno a quem só falta o nascimento: o consentimento já está registrado e o bloco não aparece. */
@Preview(name = "Concluir cadastro · aluno, só o que falta", showBackground = true, heightDp = 900)
@Composable
private fun CompleteProfileStudentPreview() {
    RunAndLiftTheme {
        CompleteProfileScreen(
            state = CompleteProfileUiState(
                role = ActiveRole.STUDENT,
                loading = false,
                askConsent = false,
                name = "Ana Ribeiro",
            ),
            form = ProfileFormState(phone = "11987654321"),
            actions = previewProfileFormActions(),
            onSubmit = {},
            onCompleted = {},
        )
    }
}
