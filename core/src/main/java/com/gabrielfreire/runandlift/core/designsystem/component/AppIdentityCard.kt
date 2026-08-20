package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Apresenta quem está usando o app: uma saudação, uma ou duas linhas de contexto e um monograma.
 *
 * É a primeira coisa da home dos dois papéis, e o que muda entre eles é só o texto — daí morar
 * aqui e não em um dos dois módulos de feature, que não se enxergam.
 *
 * **A moldura é opcional**, porque abrir uma tela e ser um item dentro dela não pedem o mesmo
 * desenho. Ver `framed`.
 *
 * **O monograma chega pronto.** Extrair a inicial de um nome parece trivial e não é: nome composto,
 * nome com preposição e nome vazio têm respostas diferentes, e essa é uma regra de produto. O design
 * system desenha o círculo; quem sabe o que escrever nele é a feature.
 *
 * @param greeting a saudação com o nome, já montada — "Olá, Ana".
 * @param subtitle a linha de baixo, em tom secundário. Tipicamente o papel de quem está logado, ou
 *   quem o acompanha.
 * @param monogram uma ou duas letras para o círculo, ou `null` quando ainda não há nome. Sem ele o
 *   círculo não é desenhado: um círculo vazio ao lado de "Olá," anuncia um carregamento que não
 *   está acontecendo.
 * @param support uma terceira linha, abaixo do subtítulo e no mesmo tom. É onde cabe o dado que
 *   qualifica o de cima — "aluno desde tal dia" logo abaixo de quem treina a pessoa — sem virar um
 *   parágrafo de duas frases na primeira coisa que se lê na tela.
 * @param framed se o bloco é desenhado sobre uma superfície própria. **Falso quando ele abre a
 *   tela**: um card no topo de uma rolagem que começa com outro card empilha duas molduras sem
 *   nada entre elas, e a identidade de quem está logado não é um item da tela — é o cabeçalho
 *   dela. Verdadeiro quando a identidade aparece no meio de outro conteúdo e precisa se separar.
 */
@Composable
fun AppIdentityCard(
    greeting: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    monogram: String? = null,
    support: String? = null,
    framed: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (framed) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(paddingValues = contentPadding(framed = framed)),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (monogram != null) {
                Monogram(text = monogram)
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)

                if (support != null) {
                    Text(text = support, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * O respiro interno do bloco.
 *
 * Sem moldura, o padding horizontal **é zero**: quem tem moldura precisa afastar o texto da borda
 * dela, e quem não tem só empurraria a saudação para dentro da margem da tela, desalinhando-a de
 * tudo o que vem abaixo.
 */
private fun contentPadding(framed: Boolean): PaddingValues = if (framed) {
    PaddingValues(all = Dimens.SpaceLarge)
} else {
    PaddingValues(vertical = Dimens.SpaceSmall)
}

@Composable
private fun Monogram(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(Dimens.AvatarSmall),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Os três casos que o componente precisa aguentar: com moldura e monograma, sem moldura e com três
 * linhas — que é como a home do aluno abre —, e sem monograma, o estado real de quem entrou pelo
 * Google e ainda não tem nome gravado, onde se confere se o bloco não fica torto sem o círculo.
 */
@LightDarkPreviews
@Composable
private fun AppIdentityCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                AppIdentityCard(
                    greeting = PreviewSamples.Identity.GREETING,
                    subtitle = PreviewSamples.Identity.ROLE_STUDENT,
                    monogram = "A",
                )
                AppIdentityCard(
                    greeting = PreviewSamples.Identity.GREETING,
                    subtitle = PreviewSamples.Identity.COACH,
                    monogram = "A",
                    support = PreviewSamples.Identity.COACH_SINCE,
                    framed = false,
                )
                AppIdentityCard(
                    greeting = PreviewSamples.Identity.GREETING,
                    subtitle = PreviewSamples.Identity.ROLE_TRAINER,
                )
            }
        }
    }
}
