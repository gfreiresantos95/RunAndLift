package com.gabrielfreire.runandlift.feature.trainer.catalog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppEmptyState
import com.gabrielfreire.runandlift.core.designsystem.component.AppListRow
import com.gabrielfreire.runandlift.core.designsystem.component.AppLoadingState
import com.gabrielfreire.runandlift.core.designsystem.component.AppScreenScaffold
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O catálogo: busca, filtros e a lista de exercícios.
 *
 * **Tocar na linha escolhe o exercício; tocar em "ver execução" abre o detalhe.** A ação principal é
 * escolher — a tela foi aberta de dentro da montagem de um dia, e mandar o treinador abrir o detalhe
 * de cada um para depois voltar e tocar em outro lugar transformaria seis exercícios em dezoito
 * toques. O detalhe existe para quando o nome não basta, e fica ao lado.
 *
 * **Três vazios diferentes, e essa é a razão de a tela ter estados nomeados.** Catálogo ausente é
 * "não há nada em disco, sincronize"; busca sem resultado é "não achei isso, tente outra coisa"; e
 * carregando é nenhuma das duas. Desenhar os três iguais mandaria o treinador apagar a busca para
 * resolver um problema de sincronização.
 *
 * Coluna rolável e não lista preguiçosa, como na carteira: são centenas de linhas, e a lista
 * preguiçosa é decisão do `:core`. O gatilho para revisar é este catálogo passar de mil itens.
 */
@Composable
internal fun CatalogScreen(
    state: CatalogUiState,
    actions: CatalogActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScreenScaffold(
        title = stringResource(R.string.trainer_catalog_title),
        modifier = modifier,
        onBack = onBack,
        backContentDescription = stringResource(R.string.trainer_action_back),
    ) {
        AppTextField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            label = stringResource(R.string.trainer_catalog_search),
            supportingText = stringResource(R.string.trainer_catalog_search_support),
        )

        CatalogFilters(state = state, actions = actions)

        if (state.filter.isActive) {
            AppTextButton(
                text = stringResource(R.string.trainer_catalog_clear_filters),
                onClick = actions.onClearFilters,
            )
        }

        when {
            state.loading -> AppLoadingState(
                contentDescription = stringResource(R.string.trainer_catalog_loading),
            )

            state.isCatalogMissing -> AppEmptyState(
                title = stringResource(R.string.trainer_catalog_missing_title),
                description = stringResource(R.string.trainer_catalog_missing),
                icon = AppIcons.Workouts,
                action = stringResource(R.string.trainer_catalog_retry),
                onAction = actions.onRetry,
            )

            state.isEmptySearch -> AppEmptyState(
                title = stringResource(R.string.trainer_catalog_no_match_title),
                description = stringResource(R.string.trainer_catalog_no_match),
                icon = AppIcons.Search,
            )

            else -> CatalogList(state = state, actions = actions)
        }
    }
}

@Composable
private fun ColumnScope.CatalogList(state: CatalogUiState, actions: CatalogActions) {
    // A contagem existe porque os filtros são muitos e é fácil fechá-los demais sem perceber: ver
    // "12 exercícios" logo acima da lista é o que faz alguém desmarcar um chip antes de concluir
    // que o catálogo é pobre.
    Text(
        text = pluralStringResource(
            R.plurals.trainer_catalog_count,
            state.exercises.size,
            state.exercises.size,
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Dimens.SpaceXSmall),
    )

    state.exercises.forEach { exercise ->
        ExerciseRow(
            exercise = exercise,
            onSelect = { actions.onSelect(exercise) },
            onOpenDetail = { actions.onOpenDetail(exercise) },
        )
    }
}

/**
 * Um exercício na lista: nome, músculos e equipamento.
 *
 * O apoio junta os músculos primários e o equipamento porque são exatamente os dois critérios pelos
 * quais um treinador decide se aquele é o exercício que ele quer — "peitoral · barra" responde a
 * pergunta sem abrir nada.
 */
@Composable
private fun ExerciseRow(exercise: Exercise, onSelect: () -> Unit, onOpenDetail: () -> Unit) {
    Column {
        AppListRow(
            title = exercise.name,
            supportingText = listOfNotNull(
                exercise.muscleGroups.joinToString(", ").takeIf { it.isNotBlank() },
                exercise.equipment,
            ).joinToString(" · "),
            onClick = onSelect,
        )
        AppTextButton(text = stringResource(R.string.trainer_catalog_detail), onClick = onOpenDetail)
    }
}

@Preview(name = "Catálogo · claro", showBackground = true, heightDp = 1000)
@Preview(
    name = "Catálogo · escuro",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CatalogScreenPreview() {
    RunAndLiftTheme {
        Column {
            CatalogScreen(state = previewCatalogState(), actions = previewCatalogActions(), onBack = {})
        }
    }
}

/** A busca que não achou nada — o vazio que se distingue do catálogo ausente. */
@Preview(name = "Catálogo sem resultado · claro", showBackground = true, heightDp = 800)
@Composable
private fun CatalogEmptySearchPreview() {
    RunAndLiftTheme {
        Column {
            CatalogScreen(
                state = previewCatalogState().copy(query = "zzz", results = emptyList()),
                actions = previewCatalogActions(),
                onBack = {},
            )
        }
    }
}
