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
 * **O indicador só aparece depois de [DelayMillis].** Essa é a parte que quase sempre falta. Os
 * documentos deste app vêm do cache do Firestore na maioria das aberturas, e a carga termina em
 * dezenas de milissegundos: um indicador imediato apareceria e sumiria num piscar, que dá a
 * impressão de instabilidade — pior do que não ter mostrado nada. Com a espera, a carga rápida não
 * mostra indicador nenhum, e só a lenta — que é a que precisa de explicação — o mostra.
 *
 * A entrada é esmaecida em vez de seca, pela mesma razão: o que aparece do nada chama mais atenção
 * do que o assunto da tela.
 *
 * @param contentDescription o que o leitor de tela anuncia. É `liveRegion`, então é falado quando o
 *   indicador entra — sem isso, quem usa TalkBack fica sem nenhum sinal de que algo está em curso.
 */
@Composable
fun AppLoadingState(contentDescription: String, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(timeMillis = DELAY_MILLIS)
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
 * O indicador já visível — o preview não espera meio segundo, então o que se confere aqui é o
 * enquadramento: centralizado na área, e não colado no topo.
 */
@LightDarkPreviews
@Composable
private fun AppLoadingStatePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppLoadingState(contentDescription = PreviewSamples.State.LOADING)
        }
    }
}
