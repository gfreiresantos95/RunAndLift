package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * As três abas do treinador, na ordem em que aparecem na barra inferior.
 *
 * São as mesmas três do aluno, e o que muda é o destino de cada uma. A repetição é deliberada: o
 * dia em que o treinador ganhar uma quarta aba — a carteira de alunos — este arquivo muda sozinho,
 * sem tocar no módulo do aluno.
 */
internal enum class TrainerTab(val route: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    HOME(TrainerRoutes.HOME, R.string.trainer_tab_home, AppIcons.Home),
    WORKOUTS(TrainerRoutes.WORKOUTS, R.string.trainer_tab_workouts, AppIcons.Workouts),
    MENU(TrainerRoutes.MENU, R.string.trainer_tab_menu, AppIcons.Menu),
}
