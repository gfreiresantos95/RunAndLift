package com.gabrielfreire.runandlift.data.local.exercise

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Acesso ao catálogo em disco.
 *
 * As consultas devolvem [Flow]: o Room reemite sozinho quando a tabela muda, que é o mecanismo que
 * faz o banco local ser a fonte de verdade da UI. Nenhuma tela precisa saber que houve
 * sincronização — ela apenas recebe a lista nova.
 */
@Dao
internal interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observeById(id: String): Flow<ExerciseEntity?>

    /**
     * Busca por nome, grupo muscular ou equipamento.
     *
     * `LIKE` com `COLLATE NOCASE` resolve o caso de uso e roda em SQLite puro. FTS seria mais
     * rápido, mas o catálogo tem centenas de linhas, não milhões — e traria uma tabela virtual e
     * uma migração a mais para ganhar nada perceptível (backlog E4-01, catálogo deliberadamente
     * enxuto).
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE name LIKE '%' || :query || '%' COLLATE NOCASE
           OR muscle_groups LIKE '%' || :query || '%' COLLATE NOCASE
           OR equipment LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun search(query: String): Flow<List<ExerciseEntity>>

    @Upsert
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE owner_id IS NULL")
    suspend fun deleteGlobalCatalog()

    /**
     * Troca o catálogo global inteiro em uma transação.
     *
     * Precisa ser atômico: sem isso, um app fechado no meio da gravação abriria com catálogo
     * parcial, e o aluno veria um treino com exercício faltando. Exercício customizado do treinador
     * (`owner_id` não nulo) sobrevive — ele não vem do catálogo global.
     */
    @Transaction
    suspend fun replaceGlobalCatalog(exercises: List<ExerciseEntity>) {
        deleteGlobalCatalog()
        upsertAll(exercises)
    }
}
