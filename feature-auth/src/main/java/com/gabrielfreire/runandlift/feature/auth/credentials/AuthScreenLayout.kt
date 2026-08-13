package com.gabrielfreire.runandlift.feature.auth.credentials

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
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
 * **O conteúdo é ancorado no topo e rola inteiro**, saída alternativa incluída. Duas decisões
 * dentro disso:
 *
 * - **Ancorado, não centralizado.** Centralizar o que não cabe na tela só muda onde o primeiro
 *   campo começa em cada aparelho: num telefone pequeno, ou com a fonte do sistema no máximo — que
 *   é o caso do público mais velho (E0-09) —, o título sai por cima e o primeiro campo desce. Começando
 *   logo abaixo da barra, a tela abre igual em todo lugar e o resto se alcança rolando.
 * - **A alternativa rola junto, em vez de ficar presa no rodapé.** Fixa, ela disputa a atenção com
 *   a ação principal desde o primeiro instante e ainda come altura útil justamente onde ela falta.
 *   No fim do conteúdo, ela aparece quando a pessoa termina de ler o que a tela pede — que é
 *   exatamente quando "isto aqui não é para mim" faz sentido como pergunta.
 *
 * **Teclado.** `imePadding` encolhe a faixa rolável em vez de deslizar a tela inteira para cima:
 * com a área de rolagem menor, o campo que recebe foco é trazido para dentro dela pelo próprio
 * `BasicTextField`. O `consumeWindowInsets` antes dele não é detalhe: sem ele o recuo da barra de
 * navegação seria contado duas vezes — uma pelo `Scaffold`, outra pelo teclado — e sobraria uma
 * faixa vazia do tamanho da barra acima do teclado.
 *
 * @param onBack `null` na tela que é raiz do fluxo, para não oferecer uma saída que não existe.
 * @param bottom saída alternativa, desenhada ao fim do conteúdo rolável.
 * @param content miolo da tela, já dentro de uma [Column] rolável.
 */
@Composable
internal fun AuthScreenLayout(
    bottom: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.auth_app_name),
                onBack = onBack,
                backContentDescription = stringResource(id = R.string.auth_back),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .consumeWindowInsets(paddingValues = innerPadding)
                .imePadding()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()

            // Respiro, e não um divisor: o que separa a ação desta tela da saída para a outra é
            // distância, não uma linha a mais para o olho processar.
            Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

            bottom()
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
        id = when (role) {
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
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
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
    val terms = stringResource(id = R.string.auth_terms_url)
    val privacy = stringResource(id = R.string.auth_privacy_url)

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
            text = stringResource(id = R.string.auth_or),
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
        text = stringResource(id = R.string.auth_google_action),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingContent = {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
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

/**
 * As peças que entrar e criar conta compartilham, fora da moldura que as posiciona.
 *
 * Estão juntas de propósito: é aqui que se vê se a etiqueta de perfil, o cabeçalho, o banner de
 * falha e o rodapé continuam parecendo do mesmo app depois de alguém mexer num token de cor. A
 * moldura em si não tem preview próprio — quem a exercita são [SignInScreen] e [SignUpScreen], com
 * conteúdo real.
 */
@LightDarkPreviews
@Composable
private fun AuthComponentsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
                    RoleChip(role = ActiveRole.STUDENT)
                    RoleChip(role = ActiveRole.TRAINER)
                }

                AuthHeadline(title = "Crie a sua conta", subtitle = "Leva menos de um minuto.")

                FailureBanner(failure = AuthFailure.EMAIL_ALREADY_IN_USE)

                OrSeparator()

                GoogleSignInButton(onClick = {}, enabled = true)

                AlternativePrompt(
                    prompt = "Ainda não tem conta?",
                    action = "Crie uma conta",
                    onClick = {},
                    enabled = true,
                )
            }
        }
    }
}

private val GOOGLE_LOGO_SIZE = 20.dp
