package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.SavedConfirmation
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentRoutes
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.studentTabBar

/**
 * Liga o início do aluno ao seu ViewModel e às abas.
 *
 * Relê o estado a cada volta para a tela (`LifecycleResumeEffect`), e não só na criação: quem
 * completa o perfil e volta precisa ver o aviso sumir. O ViewModel sobrevive à ida à edição — é o
 * mesmo destino na pilha —, então sem esta releitura o aviso continuaria lá, dizendo que falta o
 * que acabou de ser preenchido.
 */
@Composable
internal fun StudentHomeDestination(
    navController: NavHostController,
    dependencies: StudentDependencies,
    onOpenProfile: () -> Unit,
    viewModel: StudentHomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                StudentHomeViewModel(
                    authRepository = dependencies.authRepository,
                    userRepository = dependencies.userRepository,
                    studentRepository = dependencies.studentRepository,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }

    // O aviso de perfil incompleto abre a edição a partir daqui, então esta aba também é destino de
    // volta — e o recibo da gravação precisa chegar nela, não só no menu.
    SavedConfirmation(
        navController = navController,
        route = StudentRoutes.HOME,
        snackbarHostState = snackbarHostState,
        message = stringResource(R.string.student_saved),
    )

    StudentHomeScreen(
        state = state,
        tabs = studentTabBar(navController = navController, current = StudentTab.HOME),
        onOpenProfile = onOpenProfile,
        snackbarHostState = snackbarHostState,
    )
}
