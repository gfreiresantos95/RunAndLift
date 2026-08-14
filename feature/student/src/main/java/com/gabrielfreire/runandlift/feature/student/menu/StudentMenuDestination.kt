package com.gabrielfreire.runandlift.feature.student.menu

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.gabrielfreire.runandlift.feature.student.navigation.StudentDependencies
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.studentTabBar

/**
 * Liga o menu do aluno ao seu ViewModel.
 *
 * [onSignedOut] é chamado **depois** de a sessão ser encerrada, e não junto do toque: navegar antes
 * deixaria a tela de entrada visível com a sessão ainda ativa, e um retorno rápido cairia de novo
 * na home.
 */
@Composable
internal fun StudentMenuDestination(
    navController: NavHostController,
    dependencies: StudentDependencies,
    onSignedOut: () -> Unit,
    onSwitchRole: (() -> Unit)?,
    onOpenProfile: () -> Unit,
    viewModel: StudentMenuViewModel = viewModel(
        factory = viewModelFactory {
            initializer { StudentMenuViewModel(dependencies.authRepository) }
        },
    ),
) {
    StudentMenuScreen(
        tabs = studentTabBar(navController = navController, current = StudentTab.MENU),
        onOpenProfile = onOpenProfile,
        onSignOut = { viewModel.signOut(onSignedOut) },
        onSwitchRole = onSwitchRole,
    )
}
