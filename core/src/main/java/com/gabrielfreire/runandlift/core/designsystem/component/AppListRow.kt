package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Linha de lista que abre outra coisa: título, uma linha de apoio e a seta.
 *
 * Existe porque esta é a **quarta** cópia do mesmo desenho no projeto — `MenuRow` está duplicado nos
 * dois módulos de papel, `StudentRow` é uma variação dele, e a montagem de treino traria a lista de
 * programas e a de dias. O gatilho de extração que o projeto usa é a terceira cópia; esta passou.
 *
 * **A seta não é enfeite, é o que diz que a linha leva a algum lugar.** Sem ela, uma lista de
 * programas e uma lista de números se parecem, e só descobre quem é qual quem tocar. É por isso que
 * ela some quando [onClick] é nulo, em vez de ficar apagada.
 *
 * **O marcador da esquerda é opcional e curto** — o "A" do dia de treino, uma inicial. Não é ícone:
 * um dia de treino não tem desenho, tem nome, e uma letra dentro de um quadrado é o que a planilha
 * de academia já usa.
 *
 * @param leading uma ou duas letras para o marcador. Mais que isso não cabe e vira reticências.
 * @param trailing texto à direita, antes da seta — uma contagem, um estado. Fica em tom secundário
 *   porque é informação de apoio, e não a resposta que a pessoa veio buscar.
 * @param onClick nulo torna a linha um bloco de leitura: sem toque, sem seta, sem papel de botão
 *   anunciado ao leitor de tela.
 */
@Composable
fun AppListRow(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leading: String? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .heightIn(min = Dimens.MinTouchTarget)
                .padding(all = Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                Marker(text = leading)
            }

            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (supportingText != null) {
                    Text(text = supportingText, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (trailing != null) {
                Text(text = trailing, style = MaterialTheme.typography.labelLarge)
            }

            if (onClick != null) {
                Icon(
                    painter = painterResource(AppIcons.ChevronRight),
                    // A seta não acrescenta nada ao que o título já disse: o papel de botão da
                    // linha inteira é o que anuncia que ela abre alguma coisa.
                    contentDescription = null,
                    modifier = Modifier.size(size = Dimens.IconSmall),
                )
            }
        }
    }
}

/** O quadrado com a letra do dia. */
@Composable
private fun Marker(text: String) {
    Surface(
        modifier = Modifier.size(size = Dimens.AvatarSmall),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

/**
 * Os três casos: com marcador e contagem (o dia de treino), só título e apoio (o programa), e sem
 * toque — que é onde se confere que a seta some em vez de ficar apagada.
 */
@LightDarkPreviews
@Composable
private fun AppListRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                AppListRow(
                    title = PreviewSamples.Program.DAY_TITLE,
                    supportingText = PreviewSamples.Program.DAY_SUPPORT,
                    leading = PreviewSamples.Program.DAY_LABEL,
                    trailing = PreviewSamples.Program.DAY_TRAILING,
                    onClick = {},
                )
                AppListRow(
                    title = PreviewSamples.Program.NAME,
                    supportingText = PreviewSamples.Program.SUPPORT,
                    onClick = {},
                )
                AppListRow(
                    title = PreviewSamples.Program.NAME,
                    supportingText = PreviewSamples.Program.SUPPORT,
                )
            }
        }
    }
}
