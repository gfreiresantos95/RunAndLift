package com.gabrielfreire.runandlift.feature.trainer.home

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
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.previewTabs
import com.gabrielfreire.runandlift.feature.trainer.profile.MissingTrainerData

/**
 * Início do treinador: o nome do app na barra superior e o card de identidade logo abaixo.
 *
 * A tela é gêmea da home do aluno **de propósito**: o mesmo desenho, com o papel dito no card. Quem
 * usa os dois papéis reconhece onde está pela linha do card e pelo conteúdo que vem abaixo, e não
 * por um layout diferente que obrigaria a reaprender a tela.
 *
 * O que entra abaixo do aviso é a carteira de alunos, com quem treinou e quem sumiu.
 */
@Composable
internal fun TrainerHomeScreen(
    state: TrainerHomeUiState,
    tabs: List<AppBottomBarItem>,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    AppTabScaffold(
        title = stringResource(R.string.trainer_app_name),
        tabs = tabs,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        AppScreenColumn(modifier = Modifier.padding(paddingValues = innerPadding)) {
            AppIdentityCard(
                greeting = state.displayName
                    ?.let { stringResource(R.string.trainer_home_greeting, it) }
                    ?: stringResource(R.string.trainer_home_greeting_anonymous),
                subtitle = stringResource(R.string.trainer_home_role),
                monogram = state.monogram,
            )

            // Logo abaixo da identidade, e antes da carteira de alunos: é uma pendência da pessoa,
            // e não do trabalho dela. Some sozinho quando não há o que completar.
            if (state.missing.any) {
                ProfileReminderCard(missingCount = state.missing.count, onClick = onOpenProfile)
            }
        }
    }
}

@Preview(name = "Início do treinador · claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Início do treinador · escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TrainerHomeScreenPreview() {
    RunAndLiftTheme {
        TrainerHomeScreen(
            state = TrainerHomeUiState(
                loading = false,
                displayName = "Carlos Pereira",
                // Com pendência, que é o estado real de quem pulou passos no passo a passo — e o
                // único em que o aviso aparece.
                missing = MissingTrainerData(specialties = true, showcase = true),
            ),
            tabs = previewTabs(TrainerTab.HOME),
            onOpenProfile = {},
        )
    }
}
