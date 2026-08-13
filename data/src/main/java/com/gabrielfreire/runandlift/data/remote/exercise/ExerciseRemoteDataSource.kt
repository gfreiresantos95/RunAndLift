package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.Exercise

/**
 * Leitura do catálogo global no Firestore.
 *
 * Contrato de custo: **uma varredura da coleção `exercises`, e só quando a versão mudou**. Quem
 * decide se vale a pena chamar é o repositório — esta interface não tem política, só transporte.
 */
internal interface ExerciseRemoteDataSource {
    suspend fun fetchGlobalCatalog(): List<Exercise>
}
