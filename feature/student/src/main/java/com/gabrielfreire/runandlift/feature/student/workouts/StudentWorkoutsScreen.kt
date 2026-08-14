package com.gabrielfreire.runandlift.feature.student.workouts

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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.navigation.StudentTab
import com.gabrielfreire.runandlift.feature.student.navigation.previewTabs

/**
 * Treinos do aluno. Ainda sem conteúdo — a aba existe para a navegação estar completa desde já.
 *
 * O texto do vazio diz **o que vai aparecer e o que falta acontecer** para isso, em vez de um "nada
 * por aqui": quem abre esta aba sem treino precisa saber se está esperando o treinador ou se
 * esqueceu algum passo.
 *
 * **Sem botão**, ao contrário do vazio do treinador: quem monta o treino do aluno é outra pessoa, e
 * uma ação aqui prometeria um atalho que não existe.
 */
@Composable
internal fun StudentWorkoutsScreen(tabs: List<AppBottomBarItem>, modifier: Modifier = Modifier) {
    AppTabScaffold(
        title = stringResource(R.string.student_workouts_title),
        tabs = tabs,
        modifier = modifier,
    ) { innerPadding ->
        AppEmptyState(
            title = stringResource(R.string.student_workouts_empty_title),
            description = stringResource(R.string.student_workouts_empty),
            modifier = Modifier.padding(paddingValues = innerPadding),
        )
    }
}

@Preview(name = "Treinos do aluno · claro", showBackground = true, heightDp = 640)
@Preview(
    name = "Treinos do aluno · escuro",
    showBackground = true,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun StudentWorkoutsScreenPreview() {
    RunAndLiftTheme {
        StudentWorkoutsScreen(tabs = previewTabs(StudentTab.WORKOUTS))
    }
}
