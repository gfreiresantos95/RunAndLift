package com.gabrielfreire.runandlift.di

import android.content.Context
import com.gabrielfreire.runandlift.data.DataContainer
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository

/**
 * Grafo de dependências do aplicativo, criado uma vez em
 * [com.gabrielfreire.runandlift.RunAndLiftApplication] e vivo enquanto o processo existir.
 *
 * **Injeção manual, não Hilt nem Koin** (ADR-0003). Enquanto o grafo couber em um arquivo legível,
 * um framework de DI cobraria processamento de anotação sem resolver problema que exista.
 *
 * Como cresce: cada dependência vira uma propriedade `by lazy` aqui, e o ViewModel que precisa dela
 * ganha um parâmetro de construtor mais uma `viewModelFactory` que lê o container via
 * `APPLICATION_KEY`.
 *
 * Quando trocar por Hilt: quando houver escopo por papel (treinador/aluno), `HiltWorker` para a
 * fila de sincronização (E0-04), ou dependência que precise viver menos que o processo.
 */
class AppContainer(context: Context) {

    private val dataContainer = DataContainer(
        context = context,
        catalogVersionSource = RemoteConfigCatalogVersionSource(),
    )

    /** Catálogo de exercícios, servido do banco local (backlog E0-03, E4-03). */
    val exerciseRepository: ExerciseRepository get() = dataContainer.exerciseRepository
}
