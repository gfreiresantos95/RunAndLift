package com.gabrielfreire.runandlift.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataDao
import com.gabrielfreire.runandlift.data.local.catalog.CatalogMetadataEntity
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseDao
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseEntity

/**
 * Banco local — a fonte de verdade da UI (backlog §2.5, E0-03).
 *
 * O Firestore sincroniza para cá; nenhuma tela lê o Firestore direto. É essa inversão que torna o
 * offline real e responde ao D8: a academia no subsolo não tem rede, e o treino precisa abrir do
 * mesmo jeito.
 *
 * `exportSchema = true` grava o esquema em `data/schemas/`, versionado no Git. Sem esse arquivo,
 * a migração versionada (E0-13) não tem como saber do que está migrando — e o Room não consegue
 * testar migração nenhuma.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        CatalogMetadataEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class RunAndLiftDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    abstract fun catalogMetadataDao(): CatalogMetadataDao

    internal companion object {
        private const val DATABASE_NAME = "runandlift.db"

        /**
         * Sem `fallbackToDestructiveMigration`, de propósito: apagar o banco do usuário para
         * resolver mudança de esquema é exatamente a perda de dados que o D8 descreve. Quando o
         * esquema mudar, escreva a migração (E0-13).
         */
        fun create(context: Context): RunAndLiftDatabase = Room.databaseBuilder(
            context = context.applicationContext,
            klass = RunAndLiftDatabase::class.java,
            name = DATABASE_NAME,
        ).build()
    }
}
