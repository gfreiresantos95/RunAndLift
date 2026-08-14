package com.gabrielfreire.runandlift.feature.student.workouts

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.studentTabBar

/**
 * Liga a aba de treinos às abas.
 *
 * Ainda sem ViewModel: a tela não tem estado nenhum enquanto o treino não existir. O destino existe
 * mesmo assim para que o grafo continue sendo um mapa uniforme — quando houver o que carregar, o
 * ViewModel entra aqui e nada muda no grafo.
 */
@Composable
internal fun StudentWorkoutsDestination(navController: NavHostController) {
    StudentWorkoutsScreen(
        tabs = studentTabBar(navController = navController, current = StudentTab.WORKOUTS),
    )
}
