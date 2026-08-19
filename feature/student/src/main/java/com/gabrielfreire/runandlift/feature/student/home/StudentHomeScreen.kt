package com.gabrielfreire.runandlift.feature.student.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppIdentityCard
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenColumn
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.previewTabs
import com.gabrielfreire.runandlift.feature.student.profile.MissingStudentData

/**
 * Início do aluno: o nome do app na barra superior e, logo abaixo, quem está usando o app.
 *
 * A barra superior mostra o **nome do app**, e não "Início". É a tela de abertura do papel, e
 * repetir ali o rótulo da aba que já está destacada logo abaixo gastaria a única linha em que a
 * marca aparece.
 *
 * Abaixo do card vêm, nesta ordem, o aviso de cadastro incompleto e o painel: a pendência é da
 * pessoa e vale para tudo o que ela vir depois, então vem antes do que ela veio ver.
 *
 * **A tela não conhece nenhum número do painel** — ela recebe o estado e o entrega a
 * [StudentDashboardSection]. É o que fará a troca dos exemplos por treino registrado não tocar
 * neste arquivo.
 */
@Composable
internal fun StudentHomeScreen(
    state: StudentHomeUiState,
    tabs: List<AppBottomBarItem>,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.student_app_name),
        tabs = tabs,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        AppScreenColumn(modifier = Modifier.padding(paddingValues = innerPadding)) {
            AppIdentityCard(
                greeting = state.displayName
                    ?.let { stringResource(R.string.student_home_greeting, it) }
                    ?: stringResource(R.string.student_home_greeting_anonymous),
                subtitle = stringResource(R.string.student_home_role),
                monogram = state.monogram,
            )

            // Logo abaixo da identidade, e antes de tudo o que vier depois: é uma pendência da
            // pessoa, e não do treino do dia. Some sozinho quando não há o que completar.
            if (state.missing.any) {
                ProfileReminderCard(missingCount = state.missing.count, onClick = onOpenProfile)
            }

            StudentDashboardSection(dashboard = state.dashboard)
        }
    }
}

/**
 * Com nome, que é o caso de quem criou a conta pelo formulário. O estado sem nome está no preview
 * do próprio [AppIdentityCard], onde o que se confere é o card e não a tela.
 */
@Preview(name = "Início do aluno · claro", showBackground = true, heightDp = 1280)
@Preview(
    name = "Início do aluno · escuro",
    showBackground = true,
    heightDp = 1280,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentHomeScreenPreview() {
    RunAndLiftTheme {
        StudentHomeScreen(
            state = StudentHomeUiState(
                loading = false,
                displayName = "Ana Ribeiro",
                // Com pendência, que é o estado real de quem pulou passos no onboarding — e o
                // único em que o aviso aparece.
                missing = MissingStudentData(measures = true, injuries = true),
            ),
            tabs = previewTabs(StudentTab.HOME),
            onOpenProfile = {},
        )
    }
}
