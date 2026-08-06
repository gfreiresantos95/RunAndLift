package com.gabrielfreire.runandlift

import android.app.Application
import com.gabrielfreire.runandlift.di.AppContainer

/**
 * Ponto de entrada do processo. Sua única responsabilidade é montar o [AppContainer] — qualquer
 * outra coisa aqui roda antes da primeira tela e atrasa a abertura, que o produto promete em ≤2s
 * mesmo offline (backlog E6-01).
 */
class RunAndLiftApplication : Application() {

    /** Grafo de dependências. Leitura pública, escrita só aqui. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer()
    }
}
