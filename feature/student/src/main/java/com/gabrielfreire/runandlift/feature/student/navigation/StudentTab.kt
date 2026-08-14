package com.gabrielfreire.runandlift.feature.student.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.feature.student.R

/**
 * As três abas do aluno, na ordem em que aparecem na barra inferior.
 *
 * Um enum, e não três chamadas soltas montando a lista: assim adicionar uma aba é adicionar uma
 * linha aqui, e o `when` de quem depende delas passa a falhar na compilação em vez de esquecer a
 * nova em silêncio. A ordem de declaração **é** a ordem na tela.
 */
internal enum class StudentTab(val route: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    HOME(StudentRoutes.HOME, R.string.student_tab_home, AppIcons.Home),
    WORKOUTS(StudentRoutes.WORKOUTS, R.string.student_tab_workouts, AppIcons.Workouts),
    MENU(StudentRoutes.MENU, R.string.student_tab_menu, AppIcons.Menu),
}
