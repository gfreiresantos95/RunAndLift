package com.gabrielfreire.runandlift.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gabrielfreire.runandlift.data.local.exercise.ExerciseCategoryDefault

/*
 * As migrações do banco local, em ordem (backlog E0-13).
 *
 * **Não existe `fallbackToDestructiveMigration` neste projeto**, e é por isso que este arquivo
 * existe: apagar o banco de alguém para resolver mudança de esquema é exatamente a perda de dado que
 * o offline-first promete não causar. Toda versão nova entra aqui com o SQL que a leva da anterior.
 *
 * A regra ao escrever uma: o resultado precisa bater **exatamente** com o esquema que o Room espera,
 * incluindo o `DEFAULT` de cada coluna. Se divergir, o app quebra na abertura com uma mensagem de
 * "migration didn't properly handle" — e quebra em produção, porque em desenvolvimento a instalação
 * limpa nunca passa por aqui. Os esquemas exportados em `data/schemas` são o que serve de conferência.
 */

/**
 * 1 → 2: o catálogo de exercícios ganha o que a montagem de treino precisa perguntar.
 *
 * Cinco colunas, todas em `exercises`. Músculo secundário e categoria nascem com `DEFAULT` porque
 * são `NOT NULL` no esquema; nível, mecânica e força aceitam nulo, que é resposta de verdade — a
 * base de origem deixa 87 exercícios sem mecânica e 29 sem sentido de força.
 *
 * **Nada precisa ser preenchido aqui.** As linhas existentes são catálogo global, e a primeira
 * sincronização depois desta versão as substitui inteiras (`replaceGlobalCatalog`). Os valores
 * padrão existem para o intervalo entre a migração e essa sincronização, não como dado definitivo.
 */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE exercises ADD COLUMN secondary_muscle_groups TEXT NOT NULL DEFAULT ''",
        )
        db.execSQL("ALTER TABLE exercises ADD COLUMN level TEXT")
        db.execSQL("ALTER TABLE exercises ADD COLUMN mechanic TEXT")
        db.execSQL("ALTER TABLE exercises ADD COLUMN force TEXT")
        db.execSQL(
            "ALTER TABLE exercises ADD COLUMN category TEXT NOT NULL " +
                "DEFAULT '${ExerciseCategoryDefault.STRENGTH}'",
        )
    }
}

/** Todas as migrações, na ordem em que o Room as aplica. */
internal val ALL_MIGRATIONS = listOf(MIGRATION_1_2)
