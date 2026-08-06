package com.gabrielfreire.runandlift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de abertura do aplicativo — o primeiro ViewModel do projeto, e o modelo que os demais
 * seguem: estado exposto como [StateFlow] somente-leitura, mutação restrita ao ViewModel, e
 * nenhuma referência a Activity, Context ou tipo de UI.
 *
 * Ele existe porque a decisão de sair da splash é regra, não desenho: depende de sessão
 * restaurada, papel ativo conhecido e dados locais prontos. Deixar isso na Activity misturaria
 * ciclo de vida de janela com estado de aplicação, e tornaria a regra intestável.
 */
class MainViewModel : ViewModel() {

    private val _isReady = MutableStateFlow(false)

    /** Enquanto for `false`, a splash permanece na tela. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            // Ponto de extensão da abertura. Entram aqui, quando existirem:
            // restauração da sessão do Firebase Auth (E0-02), leitura do papel ativo e aquecimento
            // do Room (E0-03) — porque o grafo de navegação depende do papel, e montar o grafo
            // errado para depois trocar produz um piscar de tela na abertura.
            //
            // Regra a manter: nada de espera artificial e nada de I/O de rede bloqueante.
            _isReady.value = true
        }
    }
}
