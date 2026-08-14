package com.gabrielfreire.runandlift.feature.student.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import kotlinx.coroutines.launch

/**
 * Ações do menu do aluno. Por enquanto, sair da conta.
 *
 * Sair é uma ação de uma etapa só e sem estado a exibir: não há como falhar de um jeito que o
 * usuário possa corrigir, e o encerramento da sessão é local. Por isso não há `StateFlow` aqui — um
 * estado de "saindo" existiria para não ser visto.
 *
 * Para **onde** ir depois quem decide é `:app`, dono do grafo raiz: este módulo não conhece a rota
 * de entrada, e é isso que o mantém sem dependência do aplicativo.
 */
internal class StudentMenuViewModel(private val authRepository: AuthRepository) : ViewModel() {

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignedOut()
        }
    }
}
