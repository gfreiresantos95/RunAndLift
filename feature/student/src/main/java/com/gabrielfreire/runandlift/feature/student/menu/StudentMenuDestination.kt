package com.gabrielfreire.runandlift.feature.student.menu

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
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
 * Liga o menu do aluno ao seu ViewModel.
 *
 * [onSignedOut] é chamado **depois** de a sessão ser encerrada, e não junto do toque: navegar antes
 * deixaria a tela de entrada visível com a sessão ainda ativa, e um retorno rápido cairia de novo
 * na home.
 *
 * @param onOpen recebe a rota a abrir. Uma função para os dois destinos, e não uma por item: quem
 *   sabe navegar é o grafo, e o menu só diz para onde.
 */
@Composable
internal fun StudentMenuDestination(
    navController: NavHostController,
    dependencies: StudentDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
    onOpen: (String) -> Unit,
    viewModel: StudentMenuViewModel = viewModel(
        factory = viewModelFactory {
            initializer { StudentMenuViewModel(dependencies.authRepository) }
        },
    ),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Esta é a tela para a qual se volta depois de salvar em "Meus dados" ou no perfil de treino, e
    // é aqui que o recibo daquela gravação aparece. Ver `SavedResult`.
    SavedConfirmation(
        navController = navController,
        route = StudentRoutes.MENU,
        snackbarHostState = snackbarHostState,
        message = stringResource(R.string.student_saved),
    )

    StudentMenuScreen(
        tabs = studentTabBar(navController = navController, current = StudentTab.MENU),
        snackbarHostState = snackbarHostState,
        actions = StudentMenuActions(
            onOpenAccount = { onOpen(StudentRoutes.ACCOUNT) },
            onOpenTraining = { onOpen(StudentRoutes.PROFILE) },
            onSignOut = { viewModel.signOut(onSignedOut) },
        ),
        onSwitchRole = onSwitchRole,
    )
}
