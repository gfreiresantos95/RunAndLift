package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppChoiceChip
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * O filtro do catálogo: **uma linha fechada, quatro fileiras abertas**.
 *
 * A primeira versão desta tela mostrava as quatro fileiras sempre. Somadas ao campo de busca, elas
 * empurravam a lista — que é o que a pessoa veio ver — para fora da primeira tela, e o treinador
 * rolava por filtros que na maioria das vezes não ia usar. Fechado, o filtro ocupa uma linha e diz
 * o que precisa dizer: quantos chips estão marcados.
 *
 * **A contagem é o que torna fechar seguro.** Esconder filtros marcados sem dizer que existem faria
 * alguém concluir que o catálogo é pobre quando na verdade ele mesmo o fechou demais — que é
 * exatamente o defeito que a contagem de resultados já evitava do outro lado.
 *
 * **Fileiras que rolam na horizontal, e não uma nuvem que quebra em linhas.** São dezessete músculos
 * e treze equipamentos; em `FlowRow` cada assunto viraria três linhas, e abrir o filtro passaria a
 * cobrir a tela inteira.
 *
 * **Nenhum chip marcado significa todos.** É o oposto do que um filtro ingênuo faz, e é o que
 * permite a tela abrir com o catálogo inteiro à mostra em vez de em branco esperando o primeiro
 * toque. A regra mora em `CatalogFilter`, onde um teste a alcança.
 */
@Composable
internal fun CatalogFilters(state: CatalogUiState, actions: CatalogActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
    ) {
        FilterHeader(state = state, actions = actions)

        AnimatedVisibility(visible = state.filtersExpanded) {
            FilterRows(state = state, actions = actions)
        }
    }
}

/**
 * A linha fechada: abrir ou fechar, e limpar.
 *
 * "Limpar" só existe quando há o que limpar — um botão permanentemente sem efeito ensina a ignorar
 * aquele canto da tela. Ele fica aqui, e não escondido junto das fileiras, porque desfazer um filtro
 * precisa estar ao alcance de quem acabou de ver a lista encolher.
 */
@Composable
private fun FilterHeader(state: CatalogUiState, actions: CatalogActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextButton(
            text = if (state.activeFilterCount > 0) {
                stringResource(R.string.trainer_catalog_filters_active, state.activeFilterCount)
            } else {
                stringResource(R.string.trainer_catalog_filters)
            },
            onClick = actions.onToggleFilters,
        )

        if (state.filter.isActive) {
            AppTextButton(
                text = stringResource(R.string.trainer_catalog_clear_filters),
                onClick = actions.onClearFilters,
            )
        }
    }
}

/**
 * Os quatro assuntos de filtro, cada um numa fileira que rola.
 *
 * Múltipla escolha nas quatro: "peito ou ombro" é a pergunta que um treinador faz ao montar um dia
 * de empurrar; "peito e ombro ao mesmo tempo" devolveria quase nada.
 */
@Composable
private fun FilterRows(state: CatalogUiState, actions: CatalogActions) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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

/** Os dois estados que importam: fechado com filtro marcado, e aberto. */
@LightDarkPreviews
@Composable
private fun CatalogFiltersPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                CatalogFilters(
                    state = previewCatalogState().copy(
                        filter = CatalogFilter(muscleGroups = setOf("Peitoral", "Ombros")),
                    ),
                    actions = previewCatalogActions(),
                )
                CatalogFilters(
                    state = previewCatalogState().copy(filtersExpanded = true),
                    actions = previewCatalogActions(),
                )
            }
        }
    }
}
