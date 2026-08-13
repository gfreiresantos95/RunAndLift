package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Documentos que o cadastro precisa deixar a um toque de distância antes de pedir o aceite.
 *
 * O enum, a função que o abre ([rememberLegalDocumentOpener]) e os dois botões que o disparam
 * ([LegalLinks]) ficam **no mesmo arquivo**: são as três partes de uma coisa só, e antes estavam em
 * três lugares — o enum no fim do arquivo de contratos de ação, o abridor no meio da moldura de
 * tela, e os botões dentro do formulário de cadastro. Acrescentar um terceiro documento exigia
 * achar os três.
 */
internal enum class LegalDocument { TERMS, PRIVACY }

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

/**
 * Os dois documentos, cada um no seu botão.
 *
 * Fora do rótulo da caixa de seleção de propósito: um link dentro do alvo que também alterna a
 * caixa faz o mesmo toque significar duas coisas, e quem usa TalkBack ouve um controle que promete
 * duas ações diferentes. Separados, cada alvo faz exatamente uma.
 */
@Composable
internal fun LegalLinks(onOpen: (LegalDocument) -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        AppTextButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.auth_terms_open),
            onClick = { onOpen(LegalDocument.TERMS) },
            enabled = enabled,
        )

        AppTextButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.auth_privacy_open),
            onClick = { onOpen(LegalDocument.PRIVACY) },
            enabled = enabled,
        )
    }
}

/**
 * "Política de Privacidade" é o rótulo mais longo do fluxo e divide a linha ao meio com o outro:
 * é aqui que se vê se os dois ainda cabem lado a lado com a fonte do sistema aumentada.
 */
@LightDarkPreviews
@Composable
private fun LegalLinksPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                LegalLinks(onOpen = {}, enabled = true)
            }
        }
    }
}
