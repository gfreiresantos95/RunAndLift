package com.gabrielfreire.runandlift.data.repository

import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataDao
import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataEntity
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseDao
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseEntity
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.remote.exercise.ExerciseRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Dublês escritos à mão, e não gerados por biblioteca de mock.
 *
 * As interfaces têm poucos métodos e o que importa nos testes é **estado** — o que ficou no banco,
 * quantas vezes a rede foi chamada. Um fake com estado expressa isso de forma mais direta e legível
 * que uma cadeia de `every { } returns`, e não quebra quando a assinatura muda de forma irrelevante.
 */

internal class FakeExerciseDao(initial: List<ExerciseEntity> = emptyList()) : ExerciseDao {

    private val rows = MutableStateFlow(initial)

    /** O que está em disco agora, para asserção direta. */
    val current: List<ExerciseEntity> get() = rows.value

    override fun observeAll(): Flow<List<ExerciseEntity>> = rows.map { list -> list.sortedBy { it.name.lowercase() } }

    override fun observeById(id: String): Flow<ExerciseEntity?> = rows.map { list -> list.firstOrNull { it.id == id } }

    override fun search(query: String): Flow<List<ExerciseEntity>> = rows.map { list ->
        list.filter { entity ->
            entity.name.contains(query, ignoreCase = true) ||
                entity.muscleGroups.contains(query, ignoreCase = true) ||
                entity.equipment.orEmpty().contains(query, ignoreCase = true)
        }.sortedBy { it.name.lowercase() }
    }

    override suspend fun upsertAll(exercises: List<ExerciseEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        exercises.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun deleteGlobalCatalog() {
        rows.value = rows.value.filter { it.ownerId != null }
    }
}

internal class FakeCatalogMetadataDao(initial: CatalogMetadataEntity? = null) : CatalogMetadataDao {

    var stored: CatalogMetadataEntity? = initial
        private set

    override suspend fun get(id: Int): CatalogMetadataEntity? = stored

    override suspend fun upsert(metadata: CatalogMetadataEntity) {
        stored = metadata
    }
}

internal class FakeExerciseRemoteDataSource(private val result: Result<List<Exercise>>) : ExerciseRemoteDataSource {

    /** Quantas vezes a rede foi tocada. É o número que o orçamento de leitura protege. */
    var fetchCount: Int = 0
        private set

    override suspend fun fetchGlobalCatalog(): List<Exercise> {
        fetchCount++
        return result.getOrThrow()
    }
}
