package com.gabrielfreire.runandlift.data.repository

import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataDao
import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataEntity
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseDao
import com.gabrielfreire.runandlift.data.local.exercise.toDomain
import com.gabrielfreire.runandlift.data.local.exercise.toEntity
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.remote.catalog.CatalogVersionSource
import com.gabrielfreire.runandlift.data.remote.exercise.ExerciseRemoteDataSource
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Implementação cache-first do catálogo. É o modelo que os demais repositórios devem seguir.
 *
 * Três regras que valem para qualquer repositório deste projeto:
 *
 * 1. **Leitura nunca vai à rede.** Todo `observe*` sai do Room e mais nada. A UI recebe dado na
 *    primeira composição, com ou sem conexão.
 * 2. **Escrita remota é operação explícita**, disparada por quem controla ciclo de vida, e o
 *    resultado volta pela Flow do banco — não pelo retorno da função.
 * 3. **Falha de rede não é exceção**, é estado esperado. Ela vira valor de retorno, e o app segue
 *    com o que tem em disco.
 */
internal class OfflineFirstExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val catalogMetadataDao: CatalogMetadataDao,
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val catalogVersionSource: CatalogVersionSource,
    private val dispatchers: AppDispatchers,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : ExerciseRepository {

    override fun observeAll(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Exercise?> =
        exerciseDao.observeById(id).map { entity -> entity?.toDomain() }

    override fun search(query: String): Flow<List<Exercise>> =
        exerciseDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncIfOutdated(): CatalogSyncResult = withContext(dispatchers.io) {
        runCatching { sync() }.getOrElse { failure -> CatalogSyncResult.Failed(failure) }
    }

    private suspend fun sync(): CatalogSyncResult {
        val localVersion = catalogMetadataDao.get()?.version ?: CatalogVersionSource.NO_LOCAL_VERSION
        val remoteVersion = catalogVersionSource.latestCatalogVersion()

        if (!isDownloadWorthwhile(localVersion = localVersion, remoteVersion = remoteVersion)) {
            return CatalogSyncResult.AlreadyUpToDate(localVersion)
        }

        val exercises = remoteDataSource.fetchGlobalCatalog()

        // Catálogo remoto vazio quase certamente é erro de configuração ou de consulta, e apagar o
        // catálogo local por causa disso deixaria o aluno sem treino. Não grava, não avança versão.
        return if (exercises.isEmpty()) {
            CatalogSyncResult.AlreadyUpToDate(localVersion)
        } else {
            persist(exercises = exercises, version = remoteVersion)
            CatalogSyncResult.Updated(version = remoteVersion, exerciseCount = exercises.size)
        }
    }

    /**
     * A decisão que protege a cota, isolada e com nome.
     *
     * Versão remota desconhecida não é motivo para baixar: na dúvida, preserva-se a cota e o
     * catálogo que já está em disco.
     */
    private fun isDownloadWorthwhile(localVersion: Int, remoteVersion: Int): Boolean =
        remoteVersion != CatalogVersionSource.UNKNOWN_VERSION && remoteVersion > localVersion

    private suspend fun persist(exercises: List<Exercise>, version: Int) {
        exerciseDao.replaceGlobalCatalog(exercises.map { it.toEntity() })
        catalogMetadataDao.upsert(
            CatalogMetadataEntity(version = version, syncedAt = currentTimeMillis()),
        )
    }
}
