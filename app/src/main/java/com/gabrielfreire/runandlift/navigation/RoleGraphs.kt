package com.gabrielfreire.runandlift.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.gabrielfreire.runandlift.R
import com.gabrielfreire.runandlift.core.designsystem.Dimens

/**
 * Grafos por papel (backlog E0-08).
 *
 * São **dois grafos irmãos**, e não um só com condicionais: o papel ativo decide qual é montado, e
 * nenhuma tela de treinador existe na pilha de um aluno. Isso é o que impede vazamento de
 * funcionalidade entre papéis por engano de navegação, num app em que a mesma conta pode ser os
 * dois (§3.2).
 *
 * Hoje cada grafo tem uma tela de espera. Elas somem quando E2-06 (lista de alunos) e E6-01
 * (treino de hoje) chegarem.
 */
fun NavGraphBuilder.trainerGraph(onSwitchRole: () -> Unit, canSwitchRole: Boolean) {
    navigation(startDestination = RoleRoutes.TRAINER_HOME, route = RoleRoutes.TRAINER_GRAPH) {
        composable(RoleRoutes.TRAINER_HOME) {
            RolePlaceholderScreen(
                title = stringResource(R.string.role_trainer_home_title),
                body = stringResource(R.string.role_trainer_home_body),
                onSwitchRole = onSwitchRole.takeIf { canSwitchRole },
            )
        }
    }
}

fun NavGraphBuilder.studentGraph(onSwitchRole: () -> Unit, canSwitchRole: Boolean) {
    navigation(startDestination = RoleRoutes.STUDENT_HOME, route = RoleRoutes.STUDENT_GRAPH) {
        composable(RoleRoutes.STUDENT_HOME) {
            RolePlaceholderScreen(
                title = stringResource(R.string.role_student_home_title),
                body = stringResource(R.string.role_student_home_body),
                onSwitchRole = onSwitchRole.takeIf { canSwitchRole },
            )
        }
    }
}

@Composable
private fun RolePlaceholderScreen(
    title: String,
    body: String,
    onSwitchRole: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        RoleSwitcher(onSwitchRole = onSwitchRole)

        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
