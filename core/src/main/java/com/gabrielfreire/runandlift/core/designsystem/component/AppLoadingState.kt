package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import com.gabrielfreire.runandlift.core.designsystem.AppMotion
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import kotlinx.coroutines.delay

/**
 * O que se desenha enquanto a tela carrega.
 *
 * Existe porque as telas de formulário faziam `if (loading) return`, o que **não desenha nada**: a
 * pessoa via a barra superior sobre uma área em branco. Branco não é "carregando", é "quebrado" —
 * e é a leitura que ela faz nos dois segundos antes de o conteúdo chegar.
 *
 * **Por padrão o indicador só aparece depois de [DELAY_MILLIS].** Essa é a parte que quase sempre
 * falta. Os documentos deste app vêm do cache do Firestore na maioria das aberturas, e a carga
 * termina em dezenas de milissegundos: um indicador imediato apareceria e sumiria num piscar, que dá
 * a impressão de instabilidade — pior do que não ter mostrado nada. Com a espera, a carga rápida não
 * mostra indicador nenhum, e só a lenta — que é a que precisa de explicação — o mostra.
 *
 * A entrada é esmaecida em vez de seca, pela mesma razão: o que aparece do nada chama mais atenção
 * do que o assunto da tela.
 *
 * @param contentDescription o que o leitor de tela anuncia. É `liveRegion`, então é falado quando o
 *   indicador entra — sem isso, quem usa TalkBack fica sem nenhum sinal de que algo está em curso.
 * @param delayMillis quanto se espera antes de mostrar o indicador. [NO_LOADING_DELAY] é para a
 *   tela que **garante uma janela mínima** de espera do outro lado — o catálogo segura o estado por
 *   alguns centésimos de propósito, para trocar um filtro dizer que o app está trabalhando. Sem essa
 *   garantia do lado de quem chama, zero aqui é o piscar que o valor padrão existe para evitar; não
 *   é um jeito de "deixar o indicador mais responsivo".
 */
@Composable
fun AppLoadingState(contentDescription: String, modifier: Modifier = Modifier, delayMillis: Long = DELAY_MILLIS) {
    var visible by remember { mutableStateOf(delayMillis <= NO_LOADING_DELAY) }

    LaunchedEffect(delayMillis) {
        delay(timeMillis = delayMillis)
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(all = Dimens.SpaceXXLarge),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = AppMotion.DURATION_MEDIUM)),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.clearAndSetSemantics {
                    this.contentDescription = contentDescription
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
    }
}

/**
 * Meio segundo. Abaixo disso o indicador pisca em toda abertura com cache quente; acima, a espera
 * fica sem explicação em quem está sem rede.
 */
private const val DELAY_MILLIS = 500L

/**
 * Sem espera: o indicador aparece no mesmo quadro.
 *
 * Só para quem garante a janela mínima do outro lado — ver o parâmetro `delayMillis` de
 * [AppLoadingState]. É público porque a garantia mora na tela, e a tela precisa nomear o que está
 * pedindo: um `0L` solto na chamada leria como descuido, e é o oposto disso.
 */
const val NO_LOADING_DELAY = 0L

/**
 * O indicador já visível.
 *
 * Sem espera **para que haja o que ver**: o preview estático não roda `LaunchedEffect`, então com o
 * valor padrão esta janela renderizaria vazia e não se conferiria nada. O que se confere aqui é o
 * enquadramento — centralizado na área, e não colado no topo.
 */
@LightDarkPreviews
@Composable
private fun AppLoadingStatePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppLoadingState(
                contentDescription = PreviewSamples.State.LOADING,
                delayMillis = NO_LOADING_DELAY,
            )
        }
    }
}
