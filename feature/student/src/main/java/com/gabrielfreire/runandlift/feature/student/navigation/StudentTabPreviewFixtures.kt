package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem

/**
 * As abas para os previews, sem `NavController`.
 *
 * [studentTabBar] precisa de um controlador de navegação, que não existe dentro de um `@Preview` —
 * e é o único motivo pelo qual as três telas não conseguiriam se desenhar sozinhas na IDE. Aqui as
 * mesmas abas são montadas com o toque sem efeito.
 *
 * Os rótulos vêm de `stringResource`, e não de literais: são as mesmas palavras que a tela mostra
 * em produção, e uma segunda cópia delas envelheceria em silêncio.
 */
@Composable
internal fun previewTabs(current: StudentTab): List<AppBottomBarItem> = StudentTab.entries.map { tab ->
    AppBottomBarItem(
        label = stringResource(tab.label),
        icon = tab.icon,
        selected = tab == current,
        onClick = {},
    )
}
