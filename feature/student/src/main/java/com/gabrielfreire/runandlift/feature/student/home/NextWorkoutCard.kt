package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O treino da vez, no topo do painel.
 *
 * É a única peça da home que responde à pergunta com que a pessoa abriu o app dentro da academia —
 * "o que eu faço hoje?" —, e por isso vem antes de qualquer número. Sequência, volume e recorde
 * contam a história da semana; nenhum deles diz o que levantar agora.
 *
 * **Pintado com `primaryContainer`, e não com o cinza dos outros cards.** É a única peça do painel
 * com essa cor, e é isso que a torna o ponto para onde o olho vai sozinho ao abrir a tela.
 *
 * **O título mora aqui dentro, e é cabeçalho para o leitor de tela como se estivesse acima.** Fora
 * do card ele era uma linha inteira anunciando o que a linha de baixo já mostrava; dentro, ele é a
 * primeira coisa que se lê do próprio card. Quem ficou sem linha própria foi o "Hoje", que desceu
 * para junto do tamanho do treino: continua sendo metade da resposta, mas não precisa de um andar
 * só para si.
 *
 * Não é clicável ainda: a tela do treino não existe. Um card que parece botão e não leva a lugar
 * nenhum é pior do que um card que não parece — e quando o destino existir, o toque entra aqui.
 */
@Composable
internal fun NextWorkoutCard(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = stringResource(R.string.student_home_next_workout_title),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(text = dashboard.nextWorkoutName, style = MaterialTheme.typography.headlineSmall)
            Text(text = dashboard.nextWorkoutFocus, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(
                    R.string.student_home_next_workout_meta,
                    stringResource(R.string.student_home_next_workout_when),
                    pluralStringResource(
                        R.plurals.student_home_next_workout_size,
                        dashboard.nextWorkoutExercises,
                        dashboard.nextWorkoutExercises,
                        dashboard.nextWorkoutMinutes,
                    ),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun NextWorkoutCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                NextWorkoutCard(dashboard = StudentDashboard.SAMPLE)
            }
        }
    }
}
