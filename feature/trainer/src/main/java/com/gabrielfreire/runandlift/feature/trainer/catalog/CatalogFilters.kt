package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * As quatro fileiras de filtro do catálogo.
 *
 * **Fileiras que rolam na horizontal, e não uma nuvem que quebra em linhas.** São dezessete músculos
 * e treze equipamentos; em `FlowRow` eles ocupariam meia tela antes de a lista começar, e a lista é
 * o que a pessoa veio ver. Rolando na horizontal, cada assunto fica numa linha e o catálogo começa
 * logo abaixo.
 *
 * **Nenhum chip marcado significa todos.** É o oposto do que um filtro ingênuo faz, e é o que
 * permite a tela abrir com o catálogo inteiro à mostra em vez de em branco esperando o primeiro
 * toque. A regra mora em `CatalogFilter`, onde um teste a alcança.
 *
 * Múltipla escolha nas quatro: "peito ou ombro" é a pergunta que um treinador faz ao montar um dia
 * de empurrar; "peito e ombro ao mesmo tempo" devolveria quase nada.
 */
@Composable
internal fun CatalogFilters(state: CatalogUiState, actions: CatalogActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        FilterRow(title = stringResource(R.string.trainer_catalog_filter_category)) {
            ExerciseCategory.entries.forEach { category ->
                AppChoiceChip(
                    label = category.label(),
                    selected = category in state.filter.categories,
                    onClick = { actions.onToggleCategory(category) },
                    multiSelect = true,
                )
            }
        }

        FilterRow(title = stringResource(R.string.trainer_catalog_filter_muscle)) {
            state.muscleOptions.forEach { muscle ->
                AppChoiceChip(
                    label = muscle,
                    selected = muscle in state.filter.muscleGroups,
                    onClick = { actions.onToggleMuscle(muscle) },
                    multiSelect = true,
                )
            }
        }

        FilterRow(title = stringResource(R.string.trainer_catalog_filter_equipment)) {
            state.equipmentOptions.forEach { equipment ->
                AppChoiceChip(
                    label = equipment,
                    selected = equipment in state.filter.equipment,
                    onClick = { actions.onToggleEquipment(equipment) },
                    multiSelect = true,
                )
            }
        }

        FilterRow(title = stringResource(R.string.trainer_catalog_filter_level)) {
            TrainingLevel.entries.forEach { level ->
                AppChoiceChip(
                    label = level.label(),
                    selected = level in state.filter.levels,
                    onClick = { actions.onToggleLevel(level) },
                    multiSelect = true,
                )
            }
        }
    }
}

/**
 * Um assunto de filtro: o rótulo em cima e os chips numa linha que rola.
 *
 * Dentro de uma `Column` própria, e não solto — dois elementos no topo de um composable é o que o
 * `compose-lints` recusa (`MultipleContentEmitters`), e com razão: quem chama não teria como
 * posicionar os dois com um `Modifier` só.
 */
@Composable
private fun FilterRow(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.SpaceXSmall),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        ) {
            content()
        }
    }
}

@LightDarkPreviews
@Composable
private fun CatalogFiltersPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                CatalogFilters(state = previewCatalogState(), actions = previewCatalogActions())
            }
        }
    }
}
