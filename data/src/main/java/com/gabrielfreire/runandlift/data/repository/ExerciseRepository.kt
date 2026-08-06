package com.gabrielfreire.runandlift.data.repository

import com.gabrielfreire.runandlift.data.model.Exercise
import kotlinx.coroutines.flow.Flow

/**
 * Acesso ao catálogo de exercícios.
 *
 * A separação entre [observeAll] e [syncIfOutdated] é a estratégia cache-first inteira, e é
 * deliberada: **ler nunca toca a rede**. Quem desenha a UI observa o banco local e recebe dados na
 * primeira composição, com ou sem conexão. A sincronização é uma operação separada, disparada por
 * quem controla o ciclo de vida, e o resultado dela chega pela mesma [Flow] — não por retorno.
 *
 * Isso é o que torna o offline real em vez de "cache que às vezes funciona" (backlog §2.5, D8).
 */
interface ExerciseRepository {

    /**
     * Observa o catálogo local. Emite imediatamente com o que houver em disco, inclusive vazio, e
     * reemite a cada mudança. **Nunca vai à rede.**
     */
    fun observeAll(): Flow<List<Exercise>>

    /** Observa um exercício específico, ou `null` se não estiver no catálogo local. */
    fun observeById(id: String): Flow<Exercise?>

    /**
     * Busca local por nome, grupo muscular ou equipamento. Roda no SQLite, sem tocar a rede
     * (backlog E4-04).
     */
    fun search(query: String): Flow<List<Exercise>>

    /**
     * Baixa o catálogo **apenas se a versão remota for maior que a local**.
     *
     * É a regra 5 do orçamento de leitura (§2.4): o catálogo é o maior conjunto de dados do
     * produto e não pode ser lido de novo a cada abertura. Quando as versões batem, o custo é
     * zero leitura do Firestore.
     *
     * Não lança: falha de rede é estado normal neste produto, e o app continua com o que tem em
     * disco. O retorno diz o que aconteceu, para quem quiser informar o usuário ou medir.
     */
    suspend fun syncIfOutdated(): CatalogSyncResult
}

/** Desfecho de uma tentativa de sincronização do catálogo. */
sealed interface CatalogSyncResult {

    /** A versão local já estava em dia. Nenhuma leitura do Firestore foi gasta. */
    data class AlreadyUpToDate(val version: Int) : CatalogSyncResult

    /** O catálogo foi baixado e gravado. */
    data class Updated(val version: Int, val exerciseCount: Int) : CatalogSyncResult

    /** A sincronização falhou. O catálogo local segue utilizável. */
    data class Failed(val cause: Throwable) : CatalogSyncResult
}
