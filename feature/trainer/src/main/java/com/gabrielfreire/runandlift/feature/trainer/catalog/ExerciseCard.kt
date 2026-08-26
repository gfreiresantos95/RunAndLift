package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Um exercício do catálogo: o nome, por que ele é aquele, e a saída para a ficha.
 *
 * **A ficha se abre por um botão dentro do card, e não por um solto embaixo dele.** Antes o botão
 * ficava fora, entre uma linha e a seguinte, e a lista virava uma sequência em que não se sabia a
 * qual exercício cada botão pertencia — o de cima ou o de baixo. Dentro, ele pertence visivelmente
 * ao card que o contém, que é a única coisa que ele precisa dizer.
 *
 * **"Saiba como fazer", e não "ver execução".** A segunda descreve o conteúdo da tela de destino; a
 * primeira responde à pergunta de quem está olhando um nome que não reconhece — e é a pergunta que
 * faz alguém tocar ali.
 *
 * O apoio junta os músculos primários e o equipamento porque são exatamente os dois critérios pelos
 * quais um treinador decide se aquele é o exercício que ele quer: "peitoral · barra" responde sem
 * abrir nada.
 *
 * @param onSelect a ação principal, no card inteiro. Escolher é o que a tela veio fazer; um botão
 *   dedicado para isso dentro do card competiria com o da ficha e faria a área maior — o card —
 *   ficar sem função.
 */
@Composable
internal fun ExerciseCard(
    exercise: Exercise,
    onSelect: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onSelect)
                .padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(text = exercise.traits(), style = MaterialTheme.typography.bodyMedium)

            AppTextButton(
                text = stringResource(R.string.trainer_catalog_detail),
                onClick = onOpenDetail,
            )
        }
    }
}

/**
 * Músculos e equipamento numa linha só.
 *
 * Exercício sem equipamento declarado — são 77 na base — fica só com os músculos, em vez de ganhar
 * um "sem equipamento" que ocuparia espaço para dizer que não há o que dizer.
 */
private fun Exercise.traits(): String = listOfNotNull(
    muscleGroups.joinToString(", ").takeIf { it.isNotBlank() },
    equipment,
).joinToString(SEPARATOR)

private const val SEPARATOR = " · "

@LightDarkPreviews
@Composable
private fun ExerciseCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                ExerciseCard(exercise = previewExercises().first(), onSelect = {}, onOpenDetail = {})
                // Sem equipamento declarado: é onde se confere que a linha de apoio se vira com
                // menos do que costuma ter.
                ExerciseCard(exercise = previewExercises().last(), onSelect = {}, onOpenDetail = {})
            }
        }
    }
}
