package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * A moldura de uma tela que **não** é aba: barra superior com título e seta, conteúdo rolável, e o
 * que mais a tela precisar embaixo.
 *
 * Reúne o que "Meus dados", "Perfil de treino" e as telas de seleção escreviam cada uma à mão — o
 * `Scaffold`, a barra, a cor de fundo, a coluna rolável, o recuo — e acrescenta duas coisas que
 * nenhuma delas tinha:
 *
 * - **A barra reage à rolagem.** `pinnedScrollBehavior` mantém a barra no lugar e troca a cor do
 *   fundo dela quando há conteúdo passando por baixo. Sem isso, a barra transparente deixava o
 *   texto do formulário deslizar por trás do título, e as duas camadas viravam uma coisa ilegível.
 *   É `pinned` e não `enterAlways`: a barra carrega a seta de voltar, e uma saída que some ao rolar
 *   obriga a rolar de volta para cima para sair.
 * - **Largura limitada** no conteúdo, por [AppScreenColumn].
 *
 * @param snackbarHostState onde as confirmações aparecem, ou `null` na tela que não confirma nada.
 *   Ver [AppSnackbarHost] para a divisão entre confirmação e falha.
 * @param bottomBar ações fixas no rodapé, para a tela que as tem. Vazio por padrão — a maioria põe
 *   a ação no fim do conteúdo rolável.
 * @param content miolo, já dentro da coluna rolável e limitada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    snackbarHostState: SnackbarHostState? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
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
        bottomBar = bottomBar,
        snackbarHost = { snackbarHostState?.let { AppSnackbarHost(hostState = it) } },
    ) { innerPadding ->
        AppScreenColumn(
            modifier = Modifier.padding(paddingValues = innerPadding),
            content = content,
        )
    }
}
