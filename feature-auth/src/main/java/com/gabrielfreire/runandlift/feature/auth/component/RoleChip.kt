package com.gabrielfreire.runandlift.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.text.chipLabel

/**
 * Etiqueta do perfil em que a pessoa está — aluno ou treinador.
 *
 * `clearAndSetSemantics` remove a semântica de botão: visualmente é um chip do Material 3, mas
 * não faz nada ao ser tocado. Anunciá-lo como botão a quem usa TalkBack prometeria uma ação que
 * não existe; assim ele é lido como o rótulo que de fato é.
 *
 * O texto vem de `ActiveRole.chipLabel()`, junto das outras decisões de "o que o app diz para cada
 * perfil" — e não de um `when` escrito aqui dentro.
 */
@Composable
internal fun RoleChip(role: ActiveRole, modifier: Modifier = Modifier) {
    val label = stringResource(id = role.chipLabel())

    AssistChip(
        onClick = {},
        label = { Text(text = label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
        modifier = modifier.clearAndSetSemantics { contentDescription = label },
    )
}

/** Os dois papéis lado a lado: é assim que se vê se a etiqueta continua legível nos dois temas. */
@LightDarkPreviews
@Composable
private fun RoleChipPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = Dimens.SpaceLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                RoleChip(role = ActiveRole.STUDENT)
                RoleChip(role = ActiveRole.TRAINER)
            }
        }
    }
}
