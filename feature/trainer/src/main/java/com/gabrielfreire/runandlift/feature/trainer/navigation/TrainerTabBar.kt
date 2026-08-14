package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem

/**
 * Monta as abas do treinador para a tela que está aberta.
 *
 * Mesma mecânica do lado do aluno: tocar na aba aberta não faz nada, o estado de cada aba sobrevive
 * à troca, e [TrainerRoutes.HOME] é a âncora da pilha.
 */
@Composable
internal fun trainerTabBar(navController: NavHostController, current: TrainerTab): List<AppBottomBarItem> =
    TrainerTab.entries.map { tab ->
        AppBottomBarItem(
            label = stringResource(tab.label),
            icon = tab.icon,
            selected = tab == current,
            onClick = {
                if (tab != current) {
                    navController.navigate(tab.route) {
                        popUpTo(TrainerRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )
    }
