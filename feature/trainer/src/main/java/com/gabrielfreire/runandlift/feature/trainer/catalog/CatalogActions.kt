package com.gabrielfreire.runandlift.feature.trainer.catalog

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.TrainingLevel

/** O que a tela do catálogo faz. */
internal data class CatalogActions(
    val onQueryChange: (String) -> Unit,
    val onToggleCategory: (ExerciseCategory) -> Unit,
    val onToggleMuscle: (String) -> Unit,
    val onToggleEquipment: (String) -> Unit,
    val onToggleLevel: (TrainingLevel) -> Unit,
    val onClearFilters: () -> Unit,
    val onSelect: (Exercise) -> Unit,
    val onOpenDetail: (Exercise) -> Unit,
    val onRetry: () -> Unit,
)
