package com.gabrielfreire.runandlift.feature.student.menu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenColumn
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.previewTabs

/**
 * Menu do aluno.
 *
 * **Duas linhas de perfil, e a divisão é a dos dois documentos por trás delas.** "Meus dados" é o
 * que a conta guarda sobre a pessoa e só ela lê; "Perfil de treino" é o que o treinador vinculado
 * precisa ler para prescrever. Uma linha só para os dois esconderia essa diferença justamente de
 * quem precisa entendê-la ao decidir o que preencher.
 *
 * As duas vêm **antes** de sair e trocar de papel porque são as únicas que se usam mais de uma vez:
 * sair é raro, trocar de papel é raro, e corrigir o próprio perfil é o que traz alguém a esta aba.
 *
 * **Sair não é o botão principal.** Usa o contorno, e não o preenchimento: a ação mais destacada de
 * uma tela é aquela que se espera que a pessoa faça, e ninguém abre o app para sair dele.
 *
 * @param onSwitchRole `null` quando a conta não tem o segundo papel — o alternador some, em vez de
 *   aparecer inerte.
 * @param snackbarHostState onde a confirmação de salvamento aparece. Esta aba é a tela para a qual
 *   se volta depois de editar, e é aqui que o recibo daquela gravação chega.
 */
@Composable
internal fun StudentMenuScreen(
    tabs: List<AppBottomBarItem>,
    actions: StudentMenuActions,
    modifier: Modifier = Modifier,
    onSwitchRole: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.student_menu_title),
        tabs = tabs,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        AppScreenColumn(
            modifier = Modifier.padding(paddingValues = innerPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        ) {
            MenuSectionTitle(text = stringResource(R.string.student_menu_section_profile))

            MenuRow(
                title = stringResource(R.string.student_menu_account),
                description = stringResource(R.string.student_menu_account_description),
                onClick = actions.onOpenAccount,
            )

            MenuRow(
                title = stringResource(R.string.student_menu_training),
                description = stringResource(R.string.student_menu_training_description),
                onClick = actions.onOpenTraining,
            )

            // Terceira linha da mesma seção, e não de uma seção própria: quem treina este aluno é
            // parte de quem ele é aqui dentro, junto do cadastro e do perfil de treino.
            MenuRow(
                title = stringResource(R.string.student_menu_trainer),
                description = stringResource(R.string.student_menu_trainer_description),
                onClick = actions.onOpenTrainer,
            )

            MenuSectionTitle(
                text = stringResource(R.string.student_menu_section_account),
                modifier = Modifier.padding(top = Dimens.SpaceLarge),
            )

            if (onSwitchRole != null) {
                AppTextButton(
                    text = stringResource(R.string.student_menu_switch_role),
                    onClick = onSwitchRole,
                )
            }

            AppOutlinedButton(
                text = stringResource(R.string.student_menu_sign_out),
                onClick = actions.onSignOut,
            )
        }
    }
}

@Composable
private fun MenuSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = Dimens.SpaceXSmall),
    )
}

/**
 * Com o alternador visível, que é o caso de conta com os dois papéis — o estado com mais coisa na
 * tela, e onde o espaçamento entre as seções se confere.
 */
@Preview(name = "Menu do aluno · claro", showBackground = true, heightDp = 720)
@Preview(
    name = "Menu do aluno · escuro",
    showBackground = true,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentMenuScreenPreview() {
    RunAndLiftTheme {
        StudentMenuScreen(
            tabs = previewTabs(StudentTab.MENU),
            actions = previewStudentMenuActions(),
            onSwitchRole = {},
        )
    }
}
