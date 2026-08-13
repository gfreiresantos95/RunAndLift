package com.gabrielfreire.runandlift.feature.auth.credentials

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
import com.gabrielfreire.runandlift.feature.auth.signUpSubtitle
import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import com.gabrielfreire.runandlift.feature.auth.validation.PhoneError

/**
 * Criar conta.
 *
 * Tela própria, e não a de entrar com outros rótulos: o que ela pede não é o mesmo, e o que ela
 * promete também não. Aqui existem nome, nascimento, contato e aceite de termos; a senha anuncia a
 * regra antes do envio; e não existe recuperação de senha nem entrada por Google.
 *
 * **Só se chega aqui pela tela de entrar.** É a única porta do fluxo de criação de conta, e é o que
 * garante que o perfil escolhido nas boas-vindas continue viajando junto — abrir o cadastro por
 * outro caminho traria alguém sem perfil, que teria de responder de novo na tela seguinte.
 *
 * O "Já tem uma conta? Entrar" fica no **fim do conteúdo rolável**, e não preso no rodapé: num
 * formulário longo, uma saída fixa fica visível durante todo o preenchimento oferecendo a porta de
 * fora justamente a quem já decidiu entrar por esta.
 *
 * O título é sempre o mesmo — quem carrega o perfil é a etiqueta, não a manchete. Repetir "de
 * aluno" no título com o chip logo acima dizendo "Aluno" seria dizer a mesma coisa duas vezes.
 *
 * **A tela é a mesma para aluno e treinador**, e isso é decisão, não economia: o que se pede para
 * abrir uma conta — nome, e-mail, senha, nascimento, contato, aceite — não depende de estar
 * prescrevendo ou executando treino. Duplicá-la para acrescentar um campo faria duas telas
 * divergirem em tudo o que elas têm em comum, que é quase tudo.
 *
 * @param role perfil escolhido na abertura. Decide a finalidade declarada em cada campo de apoio,
 *   o bloco que aparece antes do aceite — aviso de dado de saúde para o aluno, registro
 *   profissional para o treinador — e a obrigatoriedade do celular.
 */
@Composable
internal fun SignUpScreen(
    state: CredentialsUiState,
    form: SignUpFormState,
    actions: SignUpActions,
    formActions: SignUpFormActions,
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
                prompt = stringResource(id = R.string.auth_prompt_has_account),
                action = stringResource(id = R.string.auth_go_to_sign_in),
                onClick = actions.onSignIn,
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
            title = stringResource(id = R.string.auth_sign_up_title),
            subtitle = stringResource(id = role.signUpSubtitle()),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        SignUpForm(
            state = state,
            form = form,
            actions = actions,
            formActions = formActions,
            role = role,
        )
    }
}

@Preview(name = "Criar conta · aluno, claro", showBackground = true, heightDp = 1500)
@Preview(
    name = "Criar conta · aluno, escuro",
    showBackground = true,
    heightDp = 1500,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SignUpPreview() {
    RunAndLiftTheme {
        SignUpScreen(
            state = previewCredentialsState(),
            form = previewStudentForm(),
            actions = previewSignUpActions(),
            formActions = previewSignUpFormActions(),
            role = ActiveRole.STUDENT,
        )
    }
}

/**
 * A mesma tela, o outro perfil. Vale abrir os dois lado a lado: o título, o rodapé e a ordem dos
 * campos não mudam — muda a etiqueta, a promessa do subtítulo e o bloco antes do aceite.
 */
@Preview(name = "Criar conta · treinador, claro", showBackground = true, heightDp = 1700)
@Preview(
    name = "Criar conta · treinador, escuro",
    showBackground = true,
    heightDp = 1700,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SignUpTrainerPreview() {
    RunAndLiftTheme {
        SignUpScreen(
            state = previewCredentialsState(),
            form = previewTrainerForm(),
            actions = previewSignUpActions(),
            formActions = previewSignUpFormActions(),
            role = ActiveRole.TRAINER,
        )
    }
}

/** Tudo o que pode dar errado de uma vez, que é como o formulário responde a um envio vazio. */
@Preview(name = "Criar conta · treinador, com erros", showBackground = true, heightDp = 1700)
@Composable
private fun SignUpFailurePreview() {
    RunAndLiftTheme {
        SignUpScreen(
            state = previewCredentialsState().copy(
                password = "",
                failure = AuthFailure.EMAIL_ALREADY_IN_USE,
            ),
            form = SignUpFormState(
                cref = PREVIEW_CREF_INCOMPLETE,
                crefError = CrefError.INVALID,
                phoneError = PhoneError.REQUIRED,
                termsMissing = true,
            ),
            actions = previewSignUpActions(),
            formActions = previewSignUpFormActions(),
            role = ActiveRole.TRAINER,
        )
    }
}
