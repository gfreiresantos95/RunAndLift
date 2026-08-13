package com.gabrielfreire.runandlift.data.local.catalog

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Acesso à linha única de [CatalogMetadataEntity].
 *
 * Separado da entidade como `ExerciseDao` é de `ExerciseEntity`: a tabela descreve o que se guarda,
 * o DAO descreve o que se pergunta a ela, e são as consultas que mudam quando a sincronização muda.
 */
@Dao
internal interface CatalogMetadataDao {

    @Query("SELECT * FROM catalog_metadata WHERE id = :id")
    suspend fun get(id: Int = CatalogMetadataEntity.SINGLE_ROW_ID): CatalogMetadataEntity?

    @Upsert
    suspend fun upsert(metadata: CatalogMetadataEntity)
}
