package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.ColorRole
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors

/**
 * Um estado dito em três canais ao mesmo tempo: a palavra, o ícone e a cor.
 *
 * É o semáforo de aderência virado componente. Ele existia como regra escrita — "cor nunca é o
 * único canal" — e como duas telas que a cumpriam à mão de jeitos diferentes; a regra que depende
 * de cada tela lembrar dela é a regra que a próxima tela esquece. Aqui os três canais vêm juntos ou
 * não vêm: não há como usar este componente e acabar com uma bolinha verde sem legenda.
 *
 * **O ícone não descreve nada para o leitor de tela**, e é de propósito: ele diz a mesma coisa que
 * a palavra ao lado, e descrever os dois faz o TalkBack anunciar o estado duas vezes. O nó é um só,
 * pelo `mergeDescendants`.
 *
 * Fica pequeno de propósito — é rótulo, não botão. Não é clicável e por isso não pede os 48 `dp`
 * de alvo de toque; quem o puser dentro de uma linha clicável já tem o alvo pela linha inteira.
 *
 * @param role o papel de cor do domínio: `ok` para quem está em dia, `attention` para quem está
 *   escorregando, `critical` para quem parou. Vem de fora porque quem sabe o que o estado significa
 *   é a tela — o design system só sabe pintar.
 * @param icon o desenho que acompanha a palavra. O padrão é [AppIcons.Alert], que serve ao que pede
 *   ação; quem anuncia estado bom passa [AppIcons.Check].
 */
@Composable
fun AppStatusLabel(
    text: String,
    role: ColorRole,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = AppIcons.Alert,
) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        color = role.container,
        contentColor = role.onContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceSmall, vertical = Dimens.SpaceXSmall),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(size = Dimens.IconSmall),
            )
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Os três estados do semáforo, um ao lado do outro. Vale abrir nos dois temas: é aqui que se
 * confere que o verde, o âmbar e o vermelho continuam distinguíveis entre si no escuro — e que,
 * quando não estiverem, a palavra ainda resolve.
 */
@LightDarkPreviews
@Composable
private fun AppStatusLabelPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                AppStatusLabel(
                    text = PreviewSamples.Dashboard.STATUS_OK,
                    role = MaterialTheme.extendedColors.ok,
                    icon = AppIcons.Check,
                )
                AppStatusLabel(
                    text = PreviewSamples.Dashboard.STATUS_ATTENTION,
                    role = MaterialTheme.extendedColors.attention,
                )
                AppStatusLabel(
                    text = PreviewSamples.Dashboard.STATUS_CRITICAL,
                    role = MaterialTheme.extendedColors.critical,
                )
            }
        }
    }
}
