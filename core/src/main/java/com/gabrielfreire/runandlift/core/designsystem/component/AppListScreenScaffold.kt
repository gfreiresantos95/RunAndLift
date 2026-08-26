package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * [AppScreenScaffold] para a tela cujo miolo é **uma lista longa**: mesma barra superior, mesma seta,
 * mesmo comportamento de rolagem — e [AppScreenLazyColumn] no lugar da coluna comum.
 *
 * **É um segundo scaffold, e não um parâmetro do primeiro**, porque o que muda não é uma opção: é o
 * tipo do conteúdo. Um recebe `@Composable ColumnScope.()`, o outro `LazyListScope.()`, e um
 * scaffold que aceitasse os dois teria de deixar os dois nulos por padrão — abrindo a porta para a
 * tela que não passa nenhum e para a que passa os dois.
 *
 * Quando usar cada um está no KDoc de [AppScreenLazyColumn]. Em resumo: formulário e ficha ficam no
 * scaffold comum; lista longa e homogênea, cujo tamanho quem escreve a tela não decide, vem para
 * este.
 *
 * A barra usa `pinnedScrollBehavior` como a do scaffold comum e pela mesma razão — ela carrega a
 * seta de voltar, e uma saída que some ao rolar obriga a rolar de volta para cima para sair. Wiring
 * interno, para nenhuma tela precisar tocar na API experimental do Material.
 *
 * @param snackbarHostState onde as confirmações aparecem, ou `null` na tela que não confirma nada.
 * @param listState exposto para a tela que precisa levar a lista de volta ao topo — depois de trocar
 *   um filtro, por exemplo, quando o item em que a pessoa estava deixou de existir.
 * @param content os itens da lista. O recuo de tela e a largura máxima já vêm aplicados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    snackbarHostState: SnackbarHostState? = null,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = title,
                onBack = onBack,
                backContentDescription = backContentDescription,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { snackbarHostState?.let { AppSnackbarHost(hostState = it) } },
    ) { innerPadding ->
        AppScreenLazyColumn(
            modifier = Modifier.padding(paddingValues = innerPadding),
            state = listState,
            content = content,
        )
    }
}
