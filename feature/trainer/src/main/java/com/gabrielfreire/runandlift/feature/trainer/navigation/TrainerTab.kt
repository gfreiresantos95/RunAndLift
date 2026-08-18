package com.gabrielfreire.runandlift.feature.trainer.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * As quatro abas do treinador, na ordem em que aparecem na barra inferior.
 *
 * **A quarta chegou, e é a carteira de alunos** — foi o que este arquivo previu quando tinha três.
 * O módulo do aluno continua com as suas três: é a fronteira de módulo fazendo o trabalho que a
 * disciplina de nomenclatura fazia antes.
 *
 * [STUDENTS] fica em segundo, antes de [WORKOUTS], porque a ordem das abas é a da rotina de quem
 * usa: o treinador abre o app para ver quem entrou e quem sumiu, e monta treino depois de saber
 * para quem. Quatro é o limite que a barra comporta com rótulo sempre visível — a quinta, se vier,
 * é sinal de que alguma delas virou tela de dentro de outra.
 */
internal enum class TrainerTab(val route: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    HOME(TrainerRoutes.HOME, R.string.trainer_tab_home, AppIcons.Home),
    STUDENTS(TrainerRoutes.STUDENTS, R.string.trainer_tab_students, AppIcons.Students),
    WORKOUTS(TrainerRoutes.WORKOUTS, R.string.trainer_tab_workouts, AppIcons.Workouts),
    MENU(TrainerRoutes.MENU, R.string.trainer_tab_menu, AppIcons.Menu),
}
