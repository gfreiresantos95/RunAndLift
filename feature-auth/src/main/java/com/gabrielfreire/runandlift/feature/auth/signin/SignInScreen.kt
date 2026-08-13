package com.gabrielfreire.runandlift.feature.auth.signin
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
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.AlternativePrompt
import com.gabrielfreire.runandlift.feature.auth.component.AuthHeadline
import com.gabrielfreire.runandlift.feature.auth.component.AuthScreenLayout
import com.gabrielfreire.runandlift.feature.auth.component.RoleChip
import com.gabrielfreire.runandlift.feature.auth.credentials.CredentialsUiState
import com.gabrielfreire.runandlift.feature.auth.credentials.previewCredentialsState

/**
 * Entrar em conta existente.
 *
 * Tela própria, e não a mesma do cadastro com rótulos trocados: os dois fluxos divergem no que
 * pedem e no que prometem. Aqui existe "esqueci minha senha" e **não** existe regra de senha —
 * anunciar o tamanho mínimo ao entrar revelaria a regra a quem tem senha antiga mais curta.
 *
 * É também **a única porta do cadastro**: o "Ainda não tem conta? Crie uma conta" ao fim do
 * conteúdo é o único caminho para o formulário de criação, e leva o perfil escolhido na abertura
 * junto. Por isso ele é um botão de texto inteiro, e não uma frase com uma palavra clicável no fim.
 *
 * Ele **rola junto com a tela** em vez de ficar preso no rodapé. Fixo, ele competia com "Entrar"
 * desde o primeiro instante e ainda ocupava altura num aparelho pequeno de teclado aberto; ao fim
 * do conteúdo, aparece depois que a pessoa viu que não tem o que preencher ali.
 *
 * @param role perfil de onde a pessoa veio, exibido como etiqueta. É o **caminho** que ela
 *   escolheu na abertura, não uma afirmação sobre a conta: quem entra com uma conta de treinador
 *   pelo caminho de aluno vai para a área de treinador, porque o papel real vem do `users/{uid}`.
 */
@Composable
internal fun SignInScreen(
    state: CredentialsUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
    role: ActiveRole? = null,
) {
    LaunchedEffect(state.authenticated) {
        if (state.authenticated) actions.onAuthenticated()
    }

    AuthScreenLayout(
        modifier = modifier,
        onBack = actions.onBack,
        bottom = {
            AlternativePrompt(
                prompt = stringResource(id = R.string.auth_prompt_no_account),
                action = stringResource(id = R.string.auth_go_to_sign_up),
                onClick = actions.onCreateAccount,
                enabled = !state.submitting,
            )
        },
    ) {
        role?.let {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                RoleChip(role = it)
            }
        }

        AuthHeadline(
            title = stringResource(id = R.string.auth_sign_in_title),
            subtitle = stringResource(id = R.string.auth_sign_in_subtitle),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        SignInForm(state = state, actions = actions)
    }
}

@Preview(name = "Entrar · claro", showBackground = true, heightDp = 900)
@Preview(
    name = "Entrar · escuro",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SignInPreview() {
    RunAndLiftTheme {
        SignInScreen(
            state = previewCredentialsState(),
            actions = previewSignInActions(),
            role = ActiveRole.STUDENT,
        )
    }
}

@Preview(name = "Entrar · falha", showBackground = true, heightDp = 900)
@Composable
private fun SignInFailurePreview() {
    RunAndLiftTheme {
        SignInScreen(
            state = previewCredentialsState().copy(failure = AuthFailure.INVALID_CREDENTIALS),
            actions = previewSignInActions(),
            role = ActiveRole.TRAINER,
        )
    }
}
