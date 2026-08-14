package com.gabrielfreire.runandlift.feature.trainer.menu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.previewTabs

/**
 * Menu do treinador: sair da conta e, para quem tem os dois papéis, trocar de papel.
 *
 * Sair usa contorno e não preenchimento, pela mesma razão do menu do aluno: ninguém abre o app para
 * sair dele, e o botão mais destacado da tela deve ser o que se espera que a pessoa faça.
 *
 * @param onSwitchRole `null` quando a conta não tem o papel de aluno.
 */
@Composable
internal fun TrainerMenuScreen(
    tabs: List<AppBottomBarItem>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchRole: (() -> Unit)? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.trainer_menu_title),
        tabs = tabs,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(paddingValues = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        ) {
            if (onSwitchRole != null) {
                AppTextButton(
                    text = stringResource(R.string.trainer_menu_switch_role),
                    onClick = onSwitchRole,
                )
            }

            AppOutlinedButton(
                text = stringResource(R.string.trainer_menu_sign_out),
                onClick = onSignOut,
            )
        }
    }
}

@Preview(name = "Menu do treinador · claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Menu do treinador · escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TrainerMenuScreenPreview() {
    RunAndLiftTheme {
        TrainerMenuScreen(
            tabs = previewTabs(TrainerTab.MENU),
            onSignOut = {},
            onSwitchRole = {},
        )
    }
}
