package com.gabrielfreire.runandlift.feature.student.workouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes

/**
 * Liga um dia do treino ao ViewModel da aba.
 *
 * A posição vem da rota e a prescrição vem da aba, que continua na pilha: **este destino não lê
 * nada**. Posição ausente ou fora da lista cai no mesmo `null`, que a tela desenha como "este dia
 * não está mais no seu treino" — ver [StudentWorkoutsUiState.day].
 */
@Composable
internal fun WorkoutDayDestination(
    navController: NavHostController,
    entry: NavBackStackEntry,
    dependencies: StudentDependencies,
    onBack: () -> Unit,
) {
    val viewModel = sharedStudentWorkoutsViewModel(
        navController = navController,
        entry = entry,
        dependencies = dependencies,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dayIndex = entry.arguments?.getInt(StudentRoutes.DAY_INDEX_ARG) ?: MISSING_DAY

    WorkoutDayScreen(day = state.day(dayIndex), onBack = onBack)
}

/**
 * A posição que nenhum dia ocupa.
 *
 * Argumento ausente não deveria acontecer — a rota o exige —, mas cair em `-1` faz a tela dizer que
 * o dia não está lá, que é o desfecho certo, em vez de abrir o primeiro dia como se fosse o pedido.
 */
private const val MISSING_DAY = -1
