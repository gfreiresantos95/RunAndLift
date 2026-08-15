package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * A tela que ainda não tem conteúdo.
 *
 * Existe porque as abas vazias eram **um parágrafo solto no canto superior esquerdo**. Um parágrafo
 * ali não parece uma explicação, parece o começo de uma lista que não carregou — e o canto superior
 * esquerdo é justamente onde o olho não procura resposta, porque é onde o conteúdo normalmente
 * começa.
 *
 * O desenho segue o que a orientação do Google pede para estado vazio, e cada parte tem função:
 *
 * - **Centralizado**, porque não há conteúdo para alinhar. Alinhar ao topo um bloco isolado deixa a
 *   tela parecendo cortada.
 * - **Ícone em círculo**, para o bloco ter peso visual suficiente para ser lido como intencional. É
 *   o mesmo desenho do monograma da home, então não é um elemento novo no vocabulário do app.
 * - **Título curto e descrição em tom secundário**: a hierarquia é o que faz a frase ser lida em
 *   duas velocidades — a primeira diz o que é, a segunda o que fazer.
 * - **Largura contida** ([TextMaxWidth]), porque linha longa centralizada é a combinação mais
 *   difícil de ler que existe.
 *
 * **A ação é opcional e existe quando há o que fazer.** Vazio que oferece um botão inerte é pior do
 * que vazio sem botão. A aba de treinos do aluno não tem ação — quem monta o treino é o treinador,
 * e a frase diz isso; a do treinador terá, quando houver o que criar.
 *
 * @param title o que está vazio, numa frase. Não é "Nada por aqui": diz o que **vai** aparecer.
 * @param description o que precisa acontecer para deixar de estar vazio.
 * @param icon desenho do círculo. Padrão é o de treinos, que é onde o vazio acontece hoje.
 * @param action rótulo do botão, ou `null` quando não há ação possível a partir desta tela.
 */
@Composable
fun AppEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: Int = AppIcons.Workouts,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = Dimens.SpaceXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Dimens.SpaceLarge, alignment = Alignment.CenterVertically),
    ) {
        Surface(
            modifier = Modifier.size(IconCircleSize),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = CircleShape,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Nulo: o título logo abaixo já diz o que o desenho ilustra, e descrevê-lo faria o
                // leitor de tela anunciar a mesma coisa duas vezes.
                Icon(painter = painterResource(icon), contentDescription = null)
            }
        }

        Column(
            modifier = Modifier.widthIn(max = TextMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (action != null) {
            AppOutlinedButton(text = action, onClick = onAction, modifier = Modifier.widthIn(max = TextMaxWidth))
        }
    }
}

private val IconCircleSize = 72.dp

/** Cerca de sessenta caracteres por linha, que é a faixa em que texto centralizado ainda se lê. */
private val TextMaxWidth = 320.dp

/**
 * Sem ação e com ação, um sob o outro. O primeiro é o caso real da aba de treinos do aluno; o
 * segundo mostra que o botão não desequilibra o bloco quando existir.
 */
@LightDarkPreviews
@Composable
private fun AppEmptyStatePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                AppEmptyState(
                    title = PreviewSamples.State.EMPTY_TITLE,
                    description = PreviewSamples.State.EMPTY_DESCRIPTION,
                )
                AppEmptyState(
                    title = PreviewSamples.State.EMPTY_TITLE,
                    description = PreviewSamples.State.EMPTY_DESCRIPTION,
                    action = PreviewSamples.State.EMPTY_ACTION,
                )
            }
        }
    }
}
