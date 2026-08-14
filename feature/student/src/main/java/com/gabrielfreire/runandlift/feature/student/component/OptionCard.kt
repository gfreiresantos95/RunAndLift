package com.gabrielfreire.runandlift.feature.student.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.minimumTouchTarget
import com.gabrielfreire.runandlift.core.designsystem.rememberSelectionHaptics
import com.gabrielfreire.runandlift.core.designsystem.selectionAppearance
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.text.description
import com.gabrielfreire.runandlift.feature.student.text.title

/**
 * Uma opção de escolha única, com rótulo e — quando ajuda — uma frase de apoio.
 *
 * **`Modifier.selectable` e não `clickable`**: com `Role.RadioButton`, o TalkBack anuncia "opção,
 * selecionada, 1 de 3" em vez de apenas "botão". A escolha é entre irmãs, e a leitura precisa dizer
 * isso.
 *
 * **Desmarcado é contorno; marcado é preenchido** — a mesma linguagem dos chips de lesão, vinda de
 * [selectionAppearance]. Antes daqui, este cartão era cinza preenchido quando desmarcado, e três
 * passos seguidos do onboarding usavam três códigos de cor diferentes para dizer a mesma coisa.
 *
 * Continua sendo **cartão e não chip**, apesar de vestir a mesma roupa: nível e objetivo têm uma
 * frase de apoio abaixo do rótulo, e um chip não tem onde colocá-la. O que se unifica é como a
 * seleção se mostra, não o formato do que se escolhe.
 */
@Composable
internal fun OptionCard(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val haptics = rememberSelectionHaptics()
    val appearance = selectionAppearance(selected = selected)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .minimumTouchTarget()
            // Só ao **passar** a marcar: retocar a opção que já estava escolhida não mudou nada, e
            // vibrar ali ensinaria que a vibração não quer dizer coisa alguma.
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = {
                    if (!selected) haptics.selected()
                    onSelect()
                },
            ),
        color = appearance.container,
        contentColor = appearance.content,
        border = appearance.border,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            description?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Selecionada e não selecionada, uma sob a outra: é a comparação que revela se o contraste basta. */
@LightDarkPreviews
@Composable
private fun OptionCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // Texto real, de `stringResource`: uma literal aqui seria uma segunda cópia de uma
                // frase que já existe, e envelheceria em silêncio quando a primeira mudasse.
                OptionCard(
                    title = TrainingLevel.INTERMEDIATE.title(),
                    description = TrainingLevel.INTERMEDIATE.description(),
                    selected = true,
                    onSelect = {},
                )
                OptionCard(
                    title = TrainingLevel.BEGINNER.title(),
                    description = TrainingLevel.BEGINNER.description(),
                    selected = false,
                    onSelect = {},
                )
            }
        }
    }
}
