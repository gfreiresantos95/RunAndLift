package com.gabrielfreire.runandlift.data.repository

import app.cash.turbine.test
import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataEntity
import com.gabrielfreire.runandlift.data.local.exercise.toEntity
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.remote.catalog.CatalogVersionSource
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contrato de comportamento do repositório cache-first.
 *
 * O que estes testes defendem não é a mecânica do Room — é a **política**: leitura não vai à rede,
 * sincronização só acontece quando a versão mudou, e falha remota nunca destrói o que está em
 * disco. É a política que sustenta o offline real (D8) e o orçamento de leitura (§2.4).
 */
class OfflineFirstExerciseRepositoryTest {

    /**
     * Um único dispatcher, passado também ao `runTest`. Criar um por papel daria dois
     * `TestCoroutineScheduler` distintos, e a biblioteca recusa misturar schedulers.
     */
    private val testDispatcher = StandardTestDispatcher()

    private val dispatchers = AppDispatchers(io = testDispatcher, default = testDispatcher)

    private fun exercise(id: String, name: String, ownerId: String? = null) = Exercise(
        id = id,
        name = name,
        muscleGroups = listOf("peito", "tríceps"),
        equipment = "barra",
        instructions = emptyList(),
        ownerId = ownerId,
    )

    private fun repository(
        dao: FakeExerciseDao = FakeExerciseDao(),
        metadataDao: FakeCatalogMetadataDao = FakeCatalogMetadataDao(),
        remote: FakeExerciseRemoteDataSource = FakeExerciseRemoteDataSource(Result.success(emptyList())),
        remoteVersion: Int = CatalogVersionSource.UNKNOWN_VERSION,
    ) = OfflineFirstExerciseRepository(
        exerciseDao = dao,
        catalogMetadataDao = metadataDao,
        remoteDataSource = remote,
        catalogVersionSource = { remoteVersion },
        dispatchers = dispatchers,
        currentTimeMillis = { FIXED_TIME },
    )

    @Test
    fun `observeAll serve do banco local sem tocar a rede`() = runTest(testDispatcher) {
        val remote = FakeExerciseRemoteDataSource(Result.success(listOf(exercise("x", "Remoto"))))
        val dao = FakeExerciseDao(listOf(exercise("1", "Supino").toEntity()))

        repository(dao = dao, remote = remote).observeAll().test {
            val emitted = awaitItem()

            assertEquals(listOf("Supino"), emitted.map { it.name })
            assertEquals("a leitura não pode custar rede", 0, remote.fetchCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAll converte entidade em modelo de dominio`() = runTest(testDispatcher) {
        val dao = FakeExerciseDao(listOf(exercise("1", "Supino").toEntity()))

        repository(dao = dao).observeAll().test {
            val first = awaitItem().single()

            assertEquals(listOf("peito", "tríceps"), first.muscleGroups)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sync nao baixa nada quando a versao local ja esta em dia`() = runTest(testDispatcher) {
        val remote = FakeExerciseRemoteDataSource(Result.success(listOf(exercise("1", "Supino"))))
        val metadataDao = FakeCatalogMetadataDao(CatalogMetadataEntity(version = 7, syncedAt = 0))

        val result = repository(metadataDao = metadataDao, remote = remote, remoteVersion = 7)
            .syncIfOutdated()

        assertEquals(CatalogSyncResult.AlreadyUpToDate(version = 7), result)
        assertEquals("versão igual não pode gastar leitura", 0, remote.fetchCount)
    }

    @Test
    fun `sync nao baixa nada quando a versao remota e desconhecida`() = runTest(testDispatcher) {
        val remote = FakeExerciseRemoteDataSource(Result.success(listOf(exercise("1", "Supino"))))

        val result = repository(
            remote = remote,
            remoteVersion = CatalogVersionSource.UNKNOWN_VERSION,
        ).syncIfOutdated()

        assertTrue(result is CatalogSyncResult.AlreadyUpToDate)
        assertEquals("na dúvida, preserva-se a cota", 0, remote.fetchCount)
    }

    @Test
    fun `sync baixa e grava quando a versao remota e maior`() = runTest(testDispatcher) {
        val dao = FakeExerciseDao()
        val metadataDao = FakeCatalogMetadataDao(CatalogMetadataEntity(version = 1, syncedAt = 0))
        val remote = FakeExerciseRemoteDataSource(
            Result.success(listOf(exercise("1", "Supino"), exercise("2", "Agachamento"))),
        )

        val result = repository(
            dao = dao,
            metadataDao = metadataDao,
            remote = remote,
            remoteVersion = 2,
        ).syncIfOutdated()

        assertEquals(CatalogSyncResult.Updated(version = 2, exerciseCount = 2), result)
        assertEquals(1, remote.fetchCount)
        assertEquals(setOf("1", "2"), dao.current.map { it.id }.toSet())
        assertEquals(CatalogMetadataEntity(version = 2, syncedAt = FIXED_TIME), metadataDao.stored)
    }

    @Test
    fun `sync preserva exercicio customizado do treinador ao trocar o catalogo global`() = runTest(testDispatcher) {
        val custom = exercise("custom", "Exercício do treinador", ownerId = "treinador-1").toEntity()
        val dao = FakeExerciseDao(listOf(custom, exercise("antigo", "Antigo").toEntity()))
        val remote = FakeExerciseRemoteDataSource(Result.success(listOf(exercise("novo", "Novo"))))

        repository(dao = dao, remote = remote, remoteVersion = 5).syncIfOutdated()

        assertEquals(setOf("custom", "novo"), dao.current.map { it.id }.toSet())
    }

    @Test
    fun `falha de rede nao destroi o catalogo local`() = runTest(testDispatcher) {
        val dao = FakeExerciseDao(listOf(exercise("1", "Supino").toEntity()))
        val metadataDao = FakeCatalogMetadataDao(CatalogMetadataEntity(version = 1, syncedAt = 0))
        val remote = FakeExerciseRemoteDataSource(Result.failure(IllegalStateException("sem rede")))

        val result = repository(
            dao = dao,
            metadataDao = metadataDao,
            remote = remote,
            remoteVersion = 9,
        ).syncIfOutdated()

        assertTrue(result is CatalogSyncResult.Failed)
        assertEquals("o catálogo em disco continua utilizável", 1, dao.current.size)
        assertEquals("a versão não pode avançar sem gravação", 1, metadataDao.stored?.version)
    }

    @Test
    fun `catalogo remoto vazio nao apaga o que esta em disco`() = runTest(testDispatcher) {
        val dao = FakeExerciseDao(listOf(exercise("1", "Supino").toEntity()))
        val remote = FakeExerciseRemoteDataSource(Result.success(emptyList()))

        val result = repository(dao = dao, remote = remote, remoteVersion = 9).syncIfOutdated()

        assertTrue(result is CatalogSyncResult.AlreadyUpToDate)
        assertEquals("resposta vazia é sintoma de erro, não de catálogo vazio", 1, dao.current.size)
    }

    @Test
    fun `busca filtra por nome e por equipamento sem tocar a rede`() = runTest(testDispatcher) {
        val remote = FakeExerciseRemoteDataSource(Result.success(emptyList()))
        val dao = FakeExerciseDao(
            listOf(
                exercise("1", "Supino reto").toEntity(),
                exercise("2", "Agachamento livre").toEntity(),
            ),
        )

        repository(dao = dao, remote = remote).search("supino").test {
            assertEquals(listOf("Supino reto"), awaitItem().map { it.name })
            assertEquals(0, remote.fetchCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val FIXED_TIME = 1_700_000_000_000L
    }
}
