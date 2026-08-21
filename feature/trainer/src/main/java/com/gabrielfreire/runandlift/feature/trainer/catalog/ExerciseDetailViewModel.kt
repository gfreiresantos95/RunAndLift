package com.gabrielfreire.runandlift.feature.trainer.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Um exercício do catálogo local.
 *
 * Lê do Room e **não vai à rede**, como todo `observe*` deste projeto — o que significa que a ficha
 * abre na academia sem sinal, que é o único lugar onde ela realmente importa.
 *
 * `null` depois de carregar quer dizer que o exercício não está no catálogo em disco. Acontece de
 * verdade: o catálogo global é republicado de fora do app, e um exercício pode sair dele entre uma
 * versão e outra. A tela diz isso em vez de mostrar uma ficha em branco.
 */
internal class ExerciseDetailViewModel(exerciseRepository: ExerciseRepository, exerciseId: String) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.observeById(exerciseId).collect { found ->
                _exercise.value = found
                _loading.value = false
            }
        }
    }
}
