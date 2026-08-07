package com.gabrielfreire.runandlift.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.R
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton

/**
 * Alternador de papel, no topo de toda tela raiz (backlog E0-08, E1-09).
 *
 * Só aparece para quem tem **os dois** papéis. Mostrá-lo a quem tem um só ofereceria uma troca que
 * não existe, e a maioria das contas tem um papel apenas.
 *
 * @param onSwitchRole `null` quando a conta não tem o segundo papel — aí nada é desenhado.
 */
@Composable
internal fun RoleSwitcher(onSwitchRole: (() -> Unit)?, modifier: Modifier = Modifier) {
    if (onSwitchRole == null) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextButton(
            text = stringResource(R.string.role_switch_action),
            onClick = onSwitchRole,
        )
    }
}
