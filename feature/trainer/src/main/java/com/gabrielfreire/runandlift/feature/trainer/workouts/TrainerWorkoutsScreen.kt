package com.gabrielfreire.runandlift.feature.trainer.workouts

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppTabScaffold
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerTab
import com.gabrielfreire.runandlift.feature.trainer.navigation.previewTabs

/**
 * Treinos do treinador — os programas que ele monta. Ainda sem conteúdo.
 *
 * O vazio fala do **próximo passo dele**, e não do que falta ao sistema: para o treinador, treino é
 * algo que ele cria, e não algo que espera receber. É a mesma vaga do aluno com a frase invertida.
 */
@Composable
internal fun TrainerWorkoutsScreen(tabs: List<AppBottomBarItem>, modifier: Modifier = Modifier) {
    AppTabScaffold(
        title = stringResource(R.string.trainer_workouts_title),
        tabs = tabs,
        modifier = modifier,
    ) { innerPadding ->
        AppEmptyState(
            title = stringResource(R.string.trainer_workouts_empty_title),
            description = stringResource(R.string.trainer_workouts_empty),
            modifier = Modifier.padding(paddingValues = innerPadding),
        )
    }
}

@Preview(name = "Treinos do treinador · claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Treinos do treinador · escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TrainerWorkoutsScreenPreview() {
    RunAndLiftTheme {
        TrainerWorkoutsScreen(tabs = previewTabs(TrainerTab.WORKOUTS))
    }
}
