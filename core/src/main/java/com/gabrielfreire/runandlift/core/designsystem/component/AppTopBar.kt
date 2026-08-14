package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.gabrielfreire.runandlift.core.R
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Barra superior do app: título ao centro e, quando houver para onde voltar, a seta à esquerda.
 *
 * Quatro decisões embutidas:
 * - **Fundo transparente enquanto o conteúdo está no topo.** A barra herda a cor da tela em vez de
 *   desenhar uma faixa própria. Numa tela de entrada, uma faixa colorida separaria o cabeçalho do
 *   conteúdo sem que houvesse dois assuntos ali — é ruído, não hierarquia.
 * - **Mas ela ganha fundo quando a tela rola** ([scrollBehavior]). É o outro lado da mesma decisão:
 *   transparente sobre conteúdo rolando, o título fica com texto passando por baixo dele, e as duas
 *   camadas se misturam em algo ilegível. Com o comportamento ligado, o Material troca a cor da
 *   faixa conforme a rolagem, e a separação aparece exatamente quando passa a ser necessária.
 * - **A seta é opcional.** Tela que é raiz do fluxo não mostra uma saída que não existe; passar
 *   `onBack = null` some com o ícone, em vez de exibir um botão inerte.
 * - **A descrição da seta vem de fora.** O design system não tem recursos de texto, pela mesma
 *   razão de [AppPasswordField]: ele não decide idioma.
 *
 * O [IconButton] do Material 3 já nasce com 48 dp de área de toque, então o piso do projeto
 * (E0-09) está atendido sem ajuste.
 *
 * @param onBack ação de voltar, ou `null` quando a tela é a raiz do fluxo.
 * @param backContentDescription o que o leitor de tela anuncia na seta. Obrigatório quando há
 *   [onBack]: ícone sem descrição é um botão mudo para quem usa TalkBack.
 * @param scrollBehavior liga a troca de fundo na rolagem. `null` nas telas que não rolam — ali a
 *   barra é transparente sempre, e ligá-lo só custaria um observador de rolagem que nunca dispara.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = backContentDescription,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

/**
 * Com e sem seta. O `Surface` por trás não é enfeite do preview: é o que revela que a barra é
 * transparente e herda a cor da tela, em vez de desenhar uma faixa própria.
 */
@OptIn(ExperimentalMaterial3Api::class)
@LightDarkPreviews
@Composable
private fun AppTopBarPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                AppTopBar(
                    title = PreviewSamples.Label.APP_NAME,
                    onBack = {},
                    backContentDescription = PreviewSamples.Action.BACK,
                )
                AppTopBar(title = PreviewSamples.Label.APP_NAME)
            }
        }
    }
}
