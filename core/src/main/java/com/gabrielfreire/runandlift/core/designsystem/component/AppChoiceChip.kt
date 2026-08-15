package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.rememberSelectionHaptics
import com.gabrielfreire.runandlift.core.designsystem.selectionAppearance

/**
 * O item de escolha do app: um chip com rótulo curto e visto quando marcado.
 *
 * É o **único** jeito de desenhar algo selecionável aqui. Antes existiam três — cartão de largura
 * inteira para nível e objetivo, quadrado para dia da semana, chip para lesão —, e três respostas
 * visuais para "isto está escolhido?" em passos que vêm um atrás do outro obrigam a pessoa a
 * reaprender o código a cada tela.
 *
 * Desenhado sobre `Surface`, e não sobre o `FilterChip` do Material, por um motivo só e que importa:
 * o `FilterChip` fixa o papel de acessibilidade em `Checkbox` internamente, depois do modificador
 * que recebe, então não há como corrigi-lo de fora. Escolha entre irmãs precisa de `RadioButton`
 * para o TalkBack anunciar "1 de 3" em vez de "caixa de seleção" — e nível, objetivo e dia não são
 * a mesma coisa que lesão. A aparência continua sendo a do chip do Material, via
 * [selectionAppearance].
 *
 * **O visto acompanha o preenchimento**, e não o substitui. São dois canais para a mesma informação:
 * quem não distingue as duas cores lê o contorno virando bloco; quem vê a lista de relance lê o
 * visto. A regra do projeto é que cor nunca carregue sozinha o recado (E0-09).
 *
 * O retorno tátil vem embutido, porque é aqui que ele faz sentido — seleção que muda estado, na
 * academia, com o olho fora da tela. Ver `rememberSelectionHaptics`.
 *
 * @param multiSelect `true` quando as escolhas são independentes (dias, lesões) e `false` quando são
 *   entre irmãs (nível, objetivo). Decide o papel anunciado e se desmarcar é possível.
 * @param contentDescription substitui o rótulo **só para o leitor de tela**. Existe para o dia da
 *   semana, que mostra "Seg" e precisa anunciar "segunda-feira": três letras lidas em voz alta são
 *   sopa de letras.
 */
@Composable
fun AppChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = false,
    contentDescription: String? = null,
) {
    val haptics = rememberSelectionHaptics()
    val appearance = selectionAppearance(selected = selected)

    val selection = if (multiSelect) {
        Modifier.toggleable(
            value = selected,
            role = Role.Checkbox,
            onValueChange = { value ->
                haptics.toggled(value)
                onClick()
            },
        )
    } else {
        Modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            // Só ao **passar** a marcar: retocar o que já estava escolhido não mudou nada, e vibrar
            // ali ensinaria que a vibração não quer dizer coisa alguma.
            onClick = {
                if (!selected) haptics.selected()
                onClick()
            },
        )
    }

    Surface(
        modifier = modifier
            // Expande a área de toque sem engordar o desenho: o chip tem 32 dp de altura e 48 dp de
            // alvo, que é a mesma regra dos ícones do app.
            .minimumInteractiveComponentSize()
            .height(ChipHeight)
            .then(selection),
        color = appearance.container,
        contentColor = appearance.content,
        border = appearance.border,
        shape = MaterialTheme.shapes.small,
    ) {
        ChipContent(label = label, selected = selected, contentDescription = contentDescription)
    }
}

/**
 * O miolo do chip.
 *
 * O `clearAndSetSemantics` vai **no conteúdo**, e não no `Surface`: assim ele troca só o texto que o
 * leitor de tela anuncia, e o estado de marcado — que vem do `selectable`/`toggleable` lá fora —
 * continua sendo anunciado junto.
 */
@Composable
private fun ChipContent(label: String, selected: Boolean, contentDescription: String?) {
    val description = contentDescription

    Row(
        modifier = Modifier
            .padding(horizontal = Dimens.SpaceMedium)
            .then(
                if (description == null) {
                    Modifier
                } else {
                    Modifier.clearAndSetSemantics { this.contentDescription = description }
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            // Nulo: o rótulo ao lado já diz o que está marcado, e o estado vem do próprio controle.
            Icon(
                painter = painterResource(AppIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(CheckIconSize),
            )
        }

        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Altura do chip do Material 3. O alvo de toque é maior, e não muda o desenho. */
private val ChipHeight = 32.dp

/** Ícone de apoio do chip no Material 3 — menor que os 24 dp de um ícone de ação. */
private val CheckIconSize = 18.dp

/**
 * Marcado e desmarcado, e uma fileira que quebra em duas linhas — que é o caso real dos dias da
 * semana e das regiões do corpo. É aqui que se confere que o chip não pula de tamanho ao ganhar o
 * visto a ponto de reorganizar a fileira inteira.
 */
@OptIn(ExperimentalLayoutApi::class)
@LightDarkPreviews
@Composable
private fun AppChoiceChipPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
                    PreviewSamples.Picker.STATES.forEachIndexed { index, label ->
                        AppChoiceChip(label = label, selected = index == 1, onClick = {})
                    }
                }
            }
        }
    }
}
