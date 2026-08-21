package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.ProgramDay
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.repository.CatalogSyncResult
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * [ExerciseRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * O catálogo é um [MutableStateFlow] e não uma lista fixa porque é assim que o de verdade se
 * comporta: `observe*` vem do Room e **reemite a cada mudança de tabela**. Um fake que devolvesse
 * `flowOf(lista)` não distinguiria a tela que reage à sincronização daquela que só leu uma vez.
 *
 * A busca filtra por nome, que é o suficiente para o que se testa aqui — o `LIKE` de verdade também
 * varre músculo e equipamento, e reproduzi-lo seria testar o `ExerciseDao`, que já tem o seu teste.
 *
 * @param syncResult o que a sincronização responde. É `var` porque "tentar de novo" só faz sentido
 *   se a segunda tentativa puder terminar diferente da primeira.
 */
internal class FakeExerciseRepository(
    exercises: List<Exercise> = emptyList(),
    var syncResult: CatalogSyncResult = CatalogSyncResult.AlreadyUpToDate(version = 1),
) : ExerciseRepository {

    private val catalog = MutableStateFlow(exercises)

    var syncCount: Int = 0
        private set

    override fun observeAll(): Flow<List<Exercise>> = catalog

    override fun observeById(id: String): Flow<Exercise?> = catalog.map { list -> list.firstOrNull { it.id == id } }

    override fun search(query: String): Flow<List<Exercise>> = catalog.map { list ->
        list.filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun syncIfOutdated(): CatalogSyncResult {
        syncCount++

        return syncResult
    }

    /** O catálogo chegando depois da tela, que é o que a sincronização faz de verdade. */
    fun publish(exercises: List<Exercise>) {
        catalog.value = exercises
    }

    companion object {

        /** Um exercício com todos os campos que os chips filtram preenchidos. */
        fun exercise(
            id: String,
            name: String = id.replaceFirstChar(Char::uppercase),
            muscle: String = "Peitoral",
            equipment: String? = "Barra",
            level: TrainingLevel? = TrainingLevel.BEGINNER,
        ) = Exercise(
            id = id,
            name = name,
            muscleGroups = listOf(muscle),
            equipment = equipment,
            instructions = listOf("Execute o movimento."),
            level = level,
            mechanic = ExerciseMechanic.COMPOUND,
            force = ExerciseForce.PUSH,
            category = ExerciseCategory.STRENGTH,
        )

        /** Um exercício já prescrito, que é o que mora dentro de um programa. */
        fun prescription(id: String = "supino") = PrescribedExercise(
            exerciseId = id,
            exerciseName = id.replaceFirstChar(Char::uppercase),
            sets = PrescribedExercise.DEFAULT_SETS,
            minReps = PrescribedExercise.DEFAULT_MIN_REPS,
            maxReps = PrescribedExercise.DEFAULT_MAX_REPS,
        )

        /** Um dia com um exercício — o mínimo para o programa poder ir para alguém. */
        fun day(label: String = "A") = ProgramDay(label = label, exercises = listOf(prescription()))
    }
}
