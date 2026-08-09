package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
 * **Teclado.** `imePadding` encolhe a faixa rolável em vez de deslizar a tela inteira para cima:
 * com a área de rolagem menor, o campo que recebe foco é trazido para dentro dela pelo próprio
 * `BasicTextField`, e o rodapé continua acima do teclado em vez de sumir atrás dele. O
 * `consumeWindowInsets` antes dele não é detalhe: sem ele o recuo da barra de navegação seria
 * contado duas vezes — uma pelo `Scaffold`, outra pelo teclado — e sobraria uma faixa vazia do
 * tamanho da barra entre o rodapé e o teclado.
 *
 * @param anchorTop ancora o miolo logo abaixo da barra superior em vez de centralizá-lo no espaço
 *   livre. Verdadeiro para conteúdo que é uma **sequência a percorrer**, como um formulário longo:
 *   centralizar o que não cabe na tela apenas desalinha o primeiro campo em cada aparelho. Falso
 *   para conteúdo curto, que centralizado fica no alcance do polegar.
 * @param bottom saída alternativa; recebe o rodapé inteiro.
 * @param content miolo da tela, já dentro de uma [Column] rolável.
 */
@Composable
internal fun AuthScreenLayout(
    onBack: () -> Unit,
    bottom: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    anchorTop: Boolean = false,
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
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.ScreenPadding),
                verticalArrangement = if (anchorTop) Arrangement.Top else Arrangement.Center,
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

/**
 * Abre Termos ou Política no navegador.
 *
 * Fica aqui, e não no ViewModel, porque abrir uma URL é ação de UI e precisa do handler do
 * Compose — ViewModel que resolve isso acaba segurando `Context`.
 *
 * Navegador do sistema em vez de uma tela interna com WebView: documento jurídico muda sem passar
 * por publicação na loja, e a versão publicada precisa ser a versão que a pessoa lê.
 */
@Composable
internal fun rememberLegalDocumentOpener(): (LegalDocument) -> Unit {
    val uriHandler = LocalUriHandler.current
    val terms = stringResource(R.string.auth_terms_url)
    val privacy = stringResource(R.string.auth_privacy_url)

    return remember(uriHandler, terms, privacy) {
        { document ->
            uriHandler.openUri(
                when (document) {
                    LegalDocument.TERMS -> terms
                    LegalDocument.PRIVACY -> privacy
                },
            )
        }
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

private val GOOGLE_LOGO_SIZE = 20.dp
