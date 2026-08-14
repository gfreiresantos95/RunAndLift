package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem

/**
 * As abas para os previews, sem `NavController` — que não existe dentro de um `@Preview`.
 *
 * Os rótulos vêm de `stringResource` e não de literais: são as mesmas palavras de produção, e uma
 * segunda cópia envelheceria em silêncio.
 */
@Composable
internal fun previewTabs(current: TrainerTab): List<AppBottomBarItem> = TrainerTab.entries.map { tab ->
    AppBottomBarItem(
        label = stringResource(tab.label),
        icon = tab.icon,
        selected = tab == current,
        onClick = {},
    )
}
