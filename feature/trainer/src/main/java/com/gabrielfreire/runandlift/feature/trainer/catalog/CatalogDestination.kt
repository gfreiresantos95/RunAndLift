package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/**
 * Liga o catálogo ao seu ViewModel.
 *
 * @param onSelect o que fazer com o exercício escolhido. Vem de fora porque o catálogo é o mesmo
 *   quando aberto de dentro da montagem — onde escolher devolve o id para o dia — e quando aberto
 *   para navegar. Ver [PickedExercise].
 */
@Composable
internal fun CatalogDestination(
    dependencies: TrainerDependencies,
    onSelect: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CatalogViewModel(exerciseRepository = dependencies.exerciseRepository) }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CatalogScreen(
        state = state,
        actions = CatalogActions(
            onQueryChange = viewModel::onQueryChange,
            onToggleCategory = viewModel::onToggleCategory,
            onToggleMuscle = viewModel::onToggleMuscle,
            onToggleEquipment = viewModel::onToggleEquipment,
            onToggleLevel = viewModel::onToggleLevel,
            onClearFilters = viewModel::onClearFilters,
            onSelect = { exercise -> onSelect(exercise.id) },
            onOpenDetail = { exercise -> onOpenDetail(exercise.id) },
            onRetry = viewModel::onRetry,
        ),
        onBack = onBack,
    )
}
