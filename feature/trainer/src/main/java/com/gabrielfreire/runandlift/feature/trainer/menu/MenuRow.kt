package com.gabrielfreire.runandlift.feature.trainer.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Uma linha de menu: título, uma frase do que ela faz, e o toque no bloco inteiro.
 *
 * A descrição não é enfeite. "Perfil" sozinho não diz se abre o que o aluno vê, o que a conta
 * guarda ou as configurações do app — e é a dúvida que faz alguém não tocar.
 *
 * `Modifier.clickable` com `Role.Button` e altura mínima de [Dimens.ListItemHeight]: o alvo é a
 * linha, e não o texto dentro dela.
 */
@Composable
internal fun MenuRow(title: String, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ListItemHeight)
            .clickable(role = Role.Button, onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@LightDarkPreviews
@Composable
private fun MenuRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                MenuRow(
                    title = stringResource(R.string.trainer_menu_profile),
                    description = stringResource(R.string.trainer_menu_profile_description),
                    onClick = {},
                )
            }
        }
    }
}
