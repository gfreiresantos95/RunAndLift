package com.gabrielfreire.runandlift.feature.student.menu

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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.previewTabs

/**
 * Menu do aluno: por enquanto, sair da conta e — para quem tem os dois papéis — trocar de papel.
 *
 * **Sair não é o botão principal da tela.** Usa o contorno, e não o preenchimento: a ação mais
 * destacada de uma tela é aquela que se espera que a pessoa faça, e ninguém abre o app para sair
 * dele. Preencher este botão o transformaria no alvo mais fácil de acertar por engano.
 *
 * O alternador de papel aparece **só para quem tem os dois** — oferecer uma troca inexistente
 * confunde a maioria, que tem um papel só. É a mesma regra que o alternador antigo já seguia, agora
 * com um lugar fixo em vez de flutuar no topo de toda tela raiz.
 *
 * @param onSwitchRole `null` quando a conta não tem o segundo papel.
 */
@Composable
internal fun StudentMenuScreen(
    tabs: List<AppBottomBarItem>,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onSwitchRole: (() -> Unit)? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.student_menu_title),
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
                    text = stringResource(R.string.student_menu_switch_role),
                    onClick = onSwitchRole,
                )
            }

            AppOutlinedButton(
                text = stringResource(R.string.student_menu_sign_out),
                onClick = onSignOut,
            )
        }
    }
}

/**
 * Com o alternador visível, que é o caso de conta com os dois papéis — o estado que tem mais coisa
 * na tela, e onde o espaçamento entre as duas ações se confere.
 */
@Preview(name = "Menu do aluno · claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Menu do aluno · escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentMenuScreenPreview() {
    RunAndLiftTheme {
        StudentMenuScreen(
            tabs = previewTabs(StudentTab.MENU),
            onSignOut = {},
            onSwitchRole = {},
        )
    }
}
