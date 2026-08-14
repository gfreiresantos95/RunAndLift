package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem

/**
 * Monta as abas do aluno para a tela que está aberta.
 *
 * Existe para que cada uma das três telas não repita a lista inteira — repetição em que bastaria
 * uma delas marcar a aba errada para a barra mentir sobre onde se está.
 *
 * **Tocar na aba já aberta não faz nada.** Sem essa guarda, o toque empilharia uma segunda cópia da
 * mesma tela e o botão voltar passaria a desfazer um passo que o usuário não deu.
 *
 * A navegação entre abas preserva o estado de cada uma (`saveState`/`restoreState`) e usa [HOME]
 * como âncora: a posição de rolagem da lista de treinos sobrevive a uma ida ao menu, e voltar de
 * qualquer aba leva ao início antes de sair do app.
 */
@Composable
internal fun studentTabBar(navController: NavHostController, current: StudentTab): List<AppBottomBarItem> =
    StudentTab.entries.map { tab ->
        AppBottomBarItem(
            label = stringResource(tab.label),
            icon = tab.icon,
            selected = tab == current,
            onClick = {
                if (tab != current) {
                    navController.navigate(tab.route) {
                        popUpTo(StudentRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
