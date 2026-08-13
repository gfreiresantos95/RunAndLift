package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.component.AppTopBar
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Moldura das telas de entrada — entrar, criar conta, recuperar senha e concluir cadastro
 * compartilham a estrutura, não o conteúdo.
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
 * Não tem preview próprio: uma moldura vazia não mostra nada que se possa conferir. Quem a exercita
 * são as telas que a usam, com conteúdo real.
 *
 * @param onBack `null` na tela que é raiz do fluxo, para não oferecer uma saída que não existe.
 * @param bottom saída alternativa, desenhada ao fim do conteúdo rolável. Vazio na tela que não tem
 *   para onde mandar ninguém — é o caso da conclusão de cadastro, de onde não se sai pela metade.
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
