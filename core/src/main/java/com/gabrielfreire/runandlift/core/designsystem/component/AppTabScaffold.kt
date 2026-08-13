package com.gabrielfreire.runandlift.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Moldura de uma tela de aba: barra superior com o título, conteúdo, e a barra inferior embaixo.
 *
 * As três abas de cada papel têm exatamente esta forma, e os dois papéis têm as mesmas três abas.
 * Sem esta moldura, o mesmo `Scaffold` seria escrito seis vezes — e bastaria uma delas esquecer o
 * `innerPadding` para o conteúdo nascer atrás da barra inferior.
 *
 * **Não há seta de voltar**, e isso é decisão: aba não é passo de fluxo. Voltar da aba de treinos
 * para a de início contradiz o modelo mental da barra inferior, em que as três são irmãs e não
 * empilhadas.
 *
 * O conteúdo recebe o [PaddingValues] do `Scaffold` e é **obrigado** a aplicá-lo — é ele que
 * mantém o texto fora das barras e das janelas do sistema, com o app em edge-to-edge.
 *
 * @param title o que a barra superior mostra. Na home é o nome do app; nas outras, o nome da tela.
 * @param tabs as abas, já com a ativa marcada.
 */
@Composable
fun AppTabScaffold(
    title: String,
    tabs: List<AppBottomBarItem>,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = title) },
        bottomBar = { AppBottomBar(items = tabs) },
        content = content,
    )
}

@Preview(name = "Aba · claro", showBackground = true, heightDp = 420)
@Preview(
    name = "Aba · escuro",
    showBackground = true,
    heightDp = 420,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppTabScaffoldPreview() {
    RunAndLiftTheme {
        AppTabScaffold(
            title = PreviewSamples.Label.APP_NAME,
            tabs = listOf(
                AppBottomBarItem(PreviewSamples.Tab.HOME, AppIcons.Home, selected = true) {},
                AppBottomBarItem(PreviewSamples.Tab.WORKOUTS, AppIcons.Workouts, selected = false) {},
                AppBottomBarItem(PreviewSamples.Tab.MENU, AppIcons.Menu, selected = false) {},
            ),
        ) { innerPadding ->
            Text(
                text = PreviewSamples.Identity.GREETING,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
