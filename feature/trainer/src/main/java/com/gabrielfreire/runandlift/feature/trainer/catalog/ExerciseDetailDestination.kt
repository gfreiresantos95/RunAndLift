package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gabrielfreire.runandlift.feature.trainer.navigation.TrainerDependencies

/** Liga a ficha do exercício ao seu ViewModel. */
@Composable
internal fun ExerciseDetailDestination(
    dependencies: TrainerDependencies,
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel(
        key = exerciseId,
        factory = viewModelFactory {
            initializer {
                ExerciseDetailViewModel(
                    exerciseRepository = dependencies.exerciseRepository,
                    exerciseId = exerciseId,
                )
            }
        },
    ),
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    ExerciseDetailScreen(exercise = exercise, loading = loading, onBack = onBack)
}
