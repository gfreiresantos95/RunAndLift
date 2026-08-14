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
 * A seleção é marcada por **cor de fundo e contorno**, e o rótulo continua legível nos dois estados
 * — cor sozinha não pode carregar a informação (E0-09). Como a lista inteira fica visível, a opção
 * marcada é sempre comparável com as outras.
 */
@Composable
internal fun OptionCard(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .minimumTouchTarget()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
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
