package com.gabrielfreire.runandlift.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Tela de escolher um item de uma lista longa, com busca.
 *
 * É uma **tela** e não um menu suspenso, e a diferença é o tamanho da lista: um `DropdownMenu` com
 * 853 municípios é uma caixinha de 200 dp de altura para rolar oitocentos nomes, sem lugar para o
 * campo de busca que torna a coisa navegável. Ocupando a tela inteira cabem a busca no topo, o alvo
 * de toque de 48 dp por linha e uma rolagem que a pessoa consegue mirar.
 *
 * O fluxo é o do velho "abrir para obter um resultado": quem chama abre esta tela, a pessoa toca num
 * item, e a tela se fecha devolvendo a escolha. Este componente não sabe **como** o resultado
 * volta — ele só chama [AppPickerActions.onSelect] e [AppPickerActions.onBack]. Quem liga uma coisa
 * na outra é o módulo de tela, que é quem conhece a navegação.
 *
 * O filtro **não** acontece aqui. [AppPickerState.Options] já chega filtrado, porque filtrar é
 * regra — casar sem acento, ignorar maiúscula — e regra em composable não se testa sem subir uma
 * tela. O que este arquivo faz é desenhar.
 *
 * Nada aqui conhece estado nem cidade: é uma lista de textos. É o que permite a mesma tela servir
 * ao cadastro e ao perfil, em dois módulos que não se enxergam.
 */
@Composable
fun AppSearchablePicker(
    texts: AppPickerTexts,
    state: AppPickerState,
    query: String,
    actions: AppPickerActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(title = texts.title, onBack = actions.onBack, backContentDescription = texts.back)
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues = padding)) {
            SearchField(texts = texts, query = query, onQueryChange = actions.onQueryChange)

            PickerBody(texts = texts, state = state, onSelect = actions.onSelect, onRetry = actions.onRetry)
        }
    }
}

/**
 * A busca fica **acima** da lista e sempre visível, e não dentro do conteúdo rolável: descer
 * duzentos nomes e não achar mais o campo é o que faz a pessoa desistir e rolar tudo na mão.
 *
 * `ImeAction.Done` porque não há próximo campo nem envio: a lista já reagiu a cada tecla, e a única
 * coisa que a tecla de ação tem a fazer é sair do teclado para descobrir a tela toda.
 */
@Composable
private fun SearchField(texts: AppPickerTexts, query: String, onQueryChange: (String) -> Unit) {
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        label = texts.searchLabel,
        modifier = Modifier.padding(horizontal = Dimens.SpaceLarge, vertical = Dimens.SpaceSmall),
        imeAction = ImeAction.Done,
        leadingContent = {
            Icon(painter = painterResource(AppIcons.Search), contentDescription = null)
        },
        trailingContent = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(painter = painterResource(AppIcons.Clear), contentDescription = texts.clearSearch)
                }
            }
        },
    )
}

/** Os três desfechos possíveis da lista. Vazio e falha são telas diferentes de propósito. */
@Composable
private fun PickerBody(texts: AppPickerTexts, state: AppPickerState, onSelect: (String) -> Unit, onRetry: () -> Unit) {
    when (state) {
        AppPickerState.Loading -> Centered { CircularProgressIndicator() }

        AppPickerState.Failed -> Centered {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                Message(text = texts.failure)
                AppButton(text = texts.retry, onClick = onRetry)
            }
        }

        is AppPickerState.Options -> if (state.items.isEmpty()) {
            Centered { Message(text = texts.empty) }
        } else {
            Options(items = state.items, onSelect = onSelect)
        }
    }
}

@Composable
private fun Options(items: List<String>, onSelect: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // A chave é o próprio texto: os nomes de município do IBGE se repetem entre estados, nunca
        // dentro de um. Sem chave, a rolagem reaproveita a linha errada ao filtrar.
        items(items = items, key = { it }) { item ->
            Option(label = item, onClick = { onSelect(item) })
            HorizontalDivider()
        }
    }
}

/**
 * Uma linha da lista.
 *
 * A altura mínima é [Dimens.MinTouchTarget] e não a do texto: a lista é rolada com o polegar, e um
 * alvo de 32 dp entre oitocentos irmãos é um toque errado a cada punhado de tentativas.
 */
@Composable
private fun Option(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpaceLarge, vertical = Dimens.SpaceMedium),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(all = Dimens.SpaceXLarge),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Preview(name = "Seleção · lista, claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Seleção · lista, escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppSearchablePickerPreview() {
    RunAndLiftTheme {
        AppSearchablePicker(
            texts = previewPickerTexts(),
            state = AppPickerState.Options(items = PreviewSamples.Picker.STATES),
            query = "",
            actions = previewPickerActions(),
        )
    }
}

/**
 * Busca sem resultado. Vale abrir junto do preview de falha: as duas telas ficariam idênticas se o
 * componente tratasse "não achei nada" e "não consegui carregar" como o mesmo caso, e a diferença
 * entre elas é a única razão de [AppPickerState] ter três estados em vez de uma lista.
 */
@Preview(name = "Seleção · busca sem resultado", showBackground = true, heightDp = 400)
@Composable
private fun AppSearchablePickerEmptyPreview() {
    RunAndLiftTheme {
        AppSearchablePicker(
            texts = previewPickerTexts(),
            state = AppPickerState.Options(items = emptyList()),
            query = PreviewSamples.Picker.QUERY_WITHOUT_MATCH,
            actions = previewPickerActions(),
        )
    }
}

/** Falha de carregamento: a frase explica, e o botão dá saída. Aviso sem botão seria beco. */
@Preview(name = "Seleção · falhou", showBackground = true, heightDp = 400)
@Composable
private fun AppSearchablePickerFailedPreview() {
    RunAndLiftTheme {
        AppSearchablePicker(
            texts = previewPickerTexts(),
            state = AppPickerState.Failed,
            query = "",
            actions = previewPickerActions(),
        )
    }
}
