package com.gabrielfreire.runandlift.data.local.catalog

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * Estado da última sincronização do catálogo. Tabela de uma linha só — daí a chave fixa.
 *
 * É o que permite cumprir a regra 5 do orçamento de leitura: sem guardar a versão baixada, não há
 * como saber que o catálogo já está em dia, e toda abertura pagaria a leitura de novo.
 */
@Entity(tableName = "catalog_metadata")
internal data class CatalogMetadataEntity(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,
    val version: Int,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long,
) {
    internal companion object {
        const val SINGLE_ROW_ID: Int = 0
    }
}

@Dao
internal interface CatalogMetadataDao {

    @Query("SELECT * FROM catalog_metadata WHERE id = :id")
    suspend fun get(id: Int = CatalogMetadataEntity.SINGLE_ROW_ID): CatalogMetadataEntity?

    @Upsert
    suspend fun upsert(metadata: CatalogMetadataEntity)
}
