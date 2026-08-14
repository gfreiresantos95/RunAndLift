package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.studentTabBar

/** Liga o início do aluno ao seu ViewModel e às abas. */
@Composable
internal fun StudentHomeDestination(
    navController: NavHostController,
    dependencies: StudentDependencies,
    viewModel: StudentHomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                StudentHomeViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StudentHomeScreen(
        state = state,
        tabs = studentTabBar(navController = navController, current = StudentTab.HOME),
    )
}
