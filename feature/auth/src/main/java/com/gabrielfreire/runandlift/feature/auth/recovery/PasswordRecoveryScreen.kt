package com.gabrielfreire.runandlift.feature.auth.recovery

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.component.AlternativePrompt
import com.gabrielfreire.runandlift.feature.auth.component.AuthHeadline
import com.gabrielfreire.runandlift.feature.auth.component.AuthScreenLayout

/**
 * Recuperar senha.
 *
 * **Vestida como entrar e criar conta**, e não mais como uma coluna solta: mesma barra superior com
 * a marca, mesmo cabeçalho de título e frase de apoio, mesmo banner para falha do servidor, mesma
 * saída alternativa ao fim do conteúdo rolável. Antes ela desenhava a própria `Column` com um
 * `Text` grande no topo e um botão de texto no fim — o resultado abria sem barra, sem `imePadding`
 * e com o título num tamanho que nenhuma outra tela do fluxo usa. Quem chega aqui vem de "esqueci
 * minha senha" na tela de entrar, e uma tela que muda de aparência no meio do caminho parece um
 * lugar em que a senha não deveria ser digitada.
 *
 * O que a padronização traz de concreto, além da aparência: o conteúdo rola, então a confirmação
 * não fica atrás do teclado num aparelho pequeno; e o campo ganha o `bringIntoView` do
 * `AppTextField` quando recebe foco.
 *
 * A saída de baixo repete o destino da seta de voltar, e isso é intencional — é o mesmo desenho do
 * cadastro, onde "Já tem uma conta? Entrar" também só desempilha. A seta é um alvo pequeno no topo;
 * depois de ler a confirmação, quem desiste está olhando para o fim da tela, não para o começo.
 */
@Composable
internal fun PasswordRecoveryScreen(
    state: PasswordRecoveryUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScreenLayout(
        modifier = modifier,
        onBack = onBack,
        bottom = {
            AlternativePrompt(
                prompt = stringResource(id = R.string.auth_prompt_remembered_password),
                action = stringResource(id = R.string.auth_go_to_sign_in),
                onClick = onBack,
                enabled = !state.submitting,
            )
        },
    ) {
        AuthHeadline(
            title = stringResource(id = R.string.auth_recovery_title),
            subtitle = stringResource(id = R.string.auth_recovery_explanation),
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        PasswordRecoveryForm(state = state, onEmailChange = onEmailChange, onSubmit = onSubmit)
    }
}

@Preview(name = "Recuperar senha · claro", showBackground = true, heightDp = 700)
@Preview(
    name = "Recuperar senha · escuro",
    showBackground = true,
    heightDp = 700,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PasswordRecoveryPreview() {
    RunAndLiftTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryUiState(email = "ana@exemplo.com"),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

/**
 * A confirmação, que é o ponto da tela: ela diz "se o e-mail estiver cadastrado" e aparece igual
 * para endereço que existe e para endereço que não existe. Preview para não deixar ninguém
 * "melhorar" esse texto e transformar a tela num verificador de quem tem conta.
 */
@Preview(name = "Recuperar senha · enviado", showBackground = true, heightDp = 700)
@Composable
private fun PasswordRecoverySentPreview() {
    RunAndLiftTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryUiState(email = "ana@exemplo.com", sent = true),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

/** Falha de verdade — sem rede. É o único caso em que tentar de novo muda alguma coisa. */
@Preview(name = "Recuperar senha · falha", showBackground = true, heightDp = 700)
@Composable
private fun PasswordRecoveryFailurePreview() {
    RunAndLiftTheme {
        PasswordRecoveryScreen(
            state = PasswordRecoveryUiState(email = "ana@exemplo.com", failure = AuthFailure.NO_NETWORK),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
