package com.gabrielfreire.runandlift.feature.student.workouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.studentTabBar

/**
 * Liga a aba de treinos ao seu ViewModel e às abas.
 *
 * O ViewModel vem de [sharedStudentWorkoutsViewModel] e não de um `viewModel()` comum, ainda que
 * esta seja a tela dona dele: é a mesma chamada que o dia faz, e mantê-las iguais é o que garante
 * que as duas peguem a mesma instância — a entrada resolvida é a desta tela em ambos os casos.
 */
@Composable
internal fun StudentWorkoutsDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: StudentDependencies,
) {
    val viewModel = sharedStudentWorkoutsViewModel(
        navController = navController,
        entry = entry,
        dependencies = dependencies,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StudentWorkoutsScreen(
        state = state,
        tabs = studentTabBar(navController = navController, current = StudentTab.WORKOUTS),
        actions = StudentWorkoutsActions(
            onOpenDay = { index -> navController.navigate(StudentRoutes.workoutDay(index)) },
            onRetry = viewModel::refresh,
        ),
    )
}
