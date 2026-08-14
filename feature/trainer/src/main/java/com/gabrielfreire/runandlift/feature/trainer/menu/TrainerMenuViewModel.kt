package com.gabrielfreire.runandlift.feature.trainer.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Ações do menu do treinador. Por enquanto, sair da conta.
 *
 * Sem `StateFlow` pela mesma razão do lado do aluno: sair é uma etapa só, local, e sem falha que o
 * usuário possa corrigir — um estado de "saindo" existiria para não ser visto.
 */
internal class TrainerMenuViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }
}
