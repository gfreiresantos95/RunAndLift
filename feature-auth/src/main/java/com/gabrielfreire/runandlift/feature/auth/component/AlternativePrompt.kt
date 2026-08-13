package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Caminho alternativo, em **um único botão de texto**: "Ainda não tem conta? Crie uma conta".
 *
 * Antes eram dois elementos, a pergunta em texto comum e a resposta em botão. O desenho parecia
 * mais honesto — só o tocável parece tocável — e falhava na prática: quem lê a pergunta mira nela,
 * erra o alvo e conclui que a tela não tem saída para o cadastro. Sendo esta a **única** porta do
 * fluxo de criação de conta, errar o alvo aqui não é um toque perdido, é um caminho perdido.
 *
 * A frase inteira vira o alvo, e a ênfase separa o que é pergunta do que é ação: a
 * [androidx.compose.ui.text.AnnotatedString] mantém um controle só para o leitor de tela, com duas
 * cores para o olho.
 */
@Composable
internal fun AlternativePrompt(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val promptColor = MaterialTheme.colorScheme.onSurfaceVariant
    val actionColor = MaterialTheme.colorScheme.primary

    val label = remember(prompt, action, promptColor, actionColor) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = promptColor, fontWeight = FontWeight.Normal)) {
                append(prompt)
            }
            append(" ")
            withStyle(SpanStyle(color = actionColor, fontWeight = FontWeight.SemiBold)) {
                append(action)
            }
        }
    }

    AppTextButton(text = label, onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth())
}

/**
 * As três saídas do fluxo, uma sob a outra.
 *
 * É o preview que precisa ser olhado **em escala de cinza**: a ênfase da segunda metade não pode
 * depender só da cor, ou a frase vira um bloco só para quem não distingue os dois tons.
 */
@LightDarkPreviews
@Composable
private fun AlternativePromptPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                AlternativePrompt(
                    prompt = stringResource(id = R.string.auth_prompt_no_account),
                    action = stringResource(id = R.string.auth_go_to_sign_up),
                    onClick = {},
                    enabled = true,
                )

                AlternativePrompt(
                    prompt = stringResource(id = R.string.auth_prompt_has_account),
                    action = stringResource(id = R.string.auth_go_to_sign_in),
                    onClick = {},
                    enabled = true,
                )

                AlternativePrompt(
                    prompt = stringResource(id = R.string.auth_prompt_remembered_password),
                    action = stringResource(id = R.string.auth_go_to_sign_in),
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
