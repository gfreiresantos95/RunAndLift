package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.contentWidth

/**
 * A coluna de conteúdo de uma tela: rolável, com recuo de tela e **largura limitada**.
 *
 * Existe porque as telas repetiam a mesma sequência de modificadores — `fillMaxSize`, o recuo do
 * `Scaffold`, o recuo da tela, `verticalScroll` — e bastava uma delas trocar a ordem para o recuo
 * ficar dentro da área rolável, ou para o conteúdo nascer atrás da barra inferior. Reunidos, a
 * ordem é uma só e está certa uma vez.
 *
 * O que ela acrescenta ao que as telas já faziam é a **largura máxima**. Num telefone nada muda,
 * porque o limite nunca é alcançado. Num tablet, num dobrável aberto ou numa janela redimensionada
 * do ChromeOS é a diferença entre um aplicativo e um telefone esticado: sem o limite, um formulário
 * de duas colunas de campos vira uma faixa de 1000 dp em que o rótulo fica num canto da tela e o
 * valor no outro, e a linha de texto passa de setenta e cinco caracteres — onde o olho se perde ao
 * voltar para o início da linha seguinte.
 *
 * A centralização vem junto e não é separável: limitar sem centralizar encostaria tudo na borda
 * esquerda, com um vazio do tamanho de um telefone à direita.
 *
 * **A rolagem fica na coluna externa, e o limite na interna.** Ao contrário, a barra de rolagem
 * apareceria no meio da tela em vez de na borda, que é onde a mão a procura.
 *
 * @param modifier vai na coluna **externa** — é onde o recuo do `Scaffold` entra.
 * @param scrollState exposto para a tela que precisa reagir à rolagem, como a que liga o
 *   comportamento de rolagem da barra superior.
 */
@Composable
fun AppScreenColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Dimens.SpaceLarge),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .contentWidth()
                .padding(paddingValues = Dimens.ScreenPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
