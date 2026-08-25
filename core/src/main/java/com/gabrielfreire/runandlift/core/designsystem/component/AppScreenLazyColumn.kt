package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.contentWidth

/**
 * A irmã preguiçosa de [AppScreenColumn]: mesma largura limitada, mesmo recuo, só as linhas visíveis
 * compostas.
 *
 * **Existe porque uma tela do produto passou do que uma coluna comum aguenta.** O catálogo de
 * exercícios tem centenas de itens, e numa `Column` rolável todos eles são compostos de uma vez —
 * o que aparecia como a tela demorando a "encher" e, pior, como um piscar a cada toque num chip de
 * filtro, porque trocar a lista recompunha as centenas de linhas de novo. Aqui só o que está na
 * tela existe, e mudar o filtro custa o que se vê.
 *
 * **Não substitui [AppScreenColumn], e a escolha entre as duas não é de gosto.** Formulário, ficha e
 * painel continuam sendo coluna comum: eles têm dezenas de filhos de tipos diferentes, e uma lista
 * preguiçosa ali só acrescentaria uma chave por item para não ganhar nada. O gatilho para esta é o
 * que o catálogo tem: **uma lista longa e homogênea**, cujo tamanho não é decidido por quem escreve
 * a tela.
 *
 * **O limite de largura vai na lista inteira, e não em cada item.** Ao contrário de
 * [AppScreenColumn], não há uma coluna interna onde aplicá-lo uma vez; pedir `Modifier.contentWidth()`
 * em cada `item {}` seria devolver às telas exatamente a repetição que o `:core` existe para tirar —
 * e bastaria esquecê-lo num item para uma linha ficar do tamanho do tablet enquanto as outras não.
 *
 * @param state exposto para a tela que precisa reagir à rolagem ou voltar ao topo.
 * @param content os itens, no `LazyListScope` de sempre. Prefira declarar `key` nos itens de uma
 *   lista de dados: é o que permite ao Compose reaproveitar o que já estava na tela quando a lista
 *   muda, em vez de refazer tudo a partir da primeira posição.
 */
@Composable
fun AppScreenLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Dimens.SpaceLarge),
    content: LazyListScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.contentWidth(),
            state = state,
            contentPadding = Dimens.ScreenPadding,
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
