package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTopBar
import com.gabrielfreire.runandlift.data.auth.AuthFailure
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.message

/**
 * Moldura das telas de entrada — entrar e criar conta compartilham a estrutura, não o conteúdo.
 *
 * Três faixas com papéis distintos: a barra superior fixa no topo, o miolo centralizado no espaço
 * livre e a saída alternativa ancorada no rodapé. Ancorar a alternativa embaixo tira do caminho do
 * olho a ação que **não** é a desta tela: quem chegou aqui para entrar não deve tropeçar em "criar
 * conta" antes de encontrar o campo de e-mail.
 *
 * O miolo rola por dentro, então o rodapé nunca é empurrado para fora — nem com o teclado aberto,
 * nem com a fonte do sistema no tamanho máximo (E0-09).
 *
 * @param bottom saída alternativa; recebe o rodapé inteiro.
 * @param content miolo da tela, já dentro de uma [Column] centralizada.
 */
@Composable
internal fun AuthScreenLayout(
    onBack: () -> Unit,
    bottom: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.auth_app_name),
                onBack = onBack,
                backContentDescription = stringResource(R.string.auth_back),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )

            Column(modifier = Modifier.padding(Dimens.ScreenPadding)) { bottom() }
        }
    }
}

/**
 * Etiqueta do perfil em que a pessoa está — aluno ou treinador.
 *
 * `clearAndSetSemantics` remove a semântica de botão: visualmente é um chip do Material 3, mas
 * não faz nada ao ser tocado. Anunciá-lo como botão a quem usa TalkBack prometeria uma ação que
 * não existe; assim ele é lido como o rótulo que de fato é.
 */
@Composable
internal fun RoleChip(role: ActiveRole, modifier: Modifier = Modifier) {
    val label = stringResource(
        when (role) {
            ActiveRole.STUDENT -> R.string.auth_role_student
            ActiveRole.TRAINER -> R.string.auth_role_trainer
        },
    )

    AssistChip(
        onClick = {},
        label = { Text(text = label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
        modifier = modifier.clearAndSetSemantics { contentDescription = label },
    )
}

/** Título e frase de apoio, centralizados como o resto do miolo. */
@Composable
internal fun AuthHeadline(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start,
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = Dimens.SpaceXSmall),
        )
    }
}

/**
 * Falha do servidor, acima do botão e não em snackbar: mensagem que some sozinha é mensagem que o
 * usuário menos digital não chega a ler (D11).
 *
 * O fundo de erro é reforço, não o recado — o texto sozinho já diz tudo, o que mantém a regra de
 * cor nunca ser o único canal (E0-09).
 */
@Composable
internal fun FailureBanner(failure: AuthFailure, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = failure.message(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(Dimens.SpaceLarge),
        )
    }
}

/** Separador entre o formulário e a entrada por Google — dois caminhos, não uma sequência. */
@Composable
internal fun OrSeparator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.auth_or),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/**
 * Entrada federada. `Image` e não `Icon`: o logotipo tem quatro cores fixas, e `Icon` aplicaria a
 * cor de conteúdo do botão por cima, o que descaracteriza a marca.
 *
 * Sem `contentDescription`: o rótulo do botão já diz "Entrar com Google", e repetir a marca no
 * ícone faria o leitor de tela anunciar a mesma coisa duas vezes.
 */
@Composable
internal fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    AppOutlinedButton(
        text = stringResource(R.string.auth_google_action),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingContent = {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(GOOGLE_LOGO_SIZE),
            )
        },
    )
}

/** Pergunta em texto comum, resposta em botão: só o que é tocável parece tocável. */
@Composable
internal fun AlternativePrompt(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AppTextButton(text = action, onClick = onClick, enabled = enabled)
    }
}

private val GOOGLE_LOGO_SIZE = 20.dp
