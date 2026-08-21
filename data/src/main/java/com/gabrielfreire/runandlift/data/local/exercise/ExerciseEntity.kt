package com.gabrielfreire.runandlift.data.local.exercise

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.TrainingLevel

/**
 * Linha da tabela de exercícios. `internal` porque tipo do Room não sai de `:data`.
 *
 * **Lista vira texto, e isso é decisão repetida de propósito.** [muscleGroups] e
 * [secondaryMuscleGroups] são gravados separados por vírgula, e [instructions] por quebra de linha.
 * A alternativa normalizada custaria três tabelas associadas e dois JOINs para resolver um problema
 * que este dado não tem: as listas são curtas, imutáveis na prática e sempre lidas inteiras. O
 * separador de cada uma é escolhido pelo que **não pode aparecer dentro do valor** — vírgula não
 * aparece em nome de músculo, e quebra de linha não aparece dentro de um passo de execução.
 */
@Entity(tableName = "exercises")
internal data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "muscle_groups")
    val muscleGroups: String,
    val equipment: String?,
    val instructions: String?,
    @ColumnInfo(name = "media_url")
    val mediaUrl: String?,
    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String?,
    @ColumnInfo(name = "owner_id")
    val ownerId: String?,
    @ColumnInfo(name = "secondary_muscle_groups", defaultValue = "")
    val secondaryMuscleGroups: String = "",
    val level: String? = null,
    val mechanic: String? = null,
    val force: String? = null,
    @ColumnInfo(defaultValue = ExerciseCategoryDefault.STRENGTH)
    val category: String = ExerciseCategoryDefault.STRENGTH,
)

/**
 * O padrão da coluna `category`, escrito uma vez.
 *
 * Existe porque `@ColumnInfo(defaultValue = …)` exige constante de tempo de compilação e a migração
 * exige o mesmo literal no SQL. Duas cópias soltas do texto "STRENGTH" divergiriam no dia em que
 * alguém renomeasse o valor do enum, e o Room só reclamaria na primeira execução em aparelho.
 */
internal object ExerciseCategoryDefault {
    const val STRENGTH = "STRENGTH"
}

private const val LIST_SEPARATOR = ","
private const val STEP_SEPARATOR = "\n"

private fun String.toList(separator: String): List<String> =
    split(separator).map { it.trim() }.filter { it.isNotBlank() }

internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    muscleGroups = muscleGroups.toList(LIST_SEPARATOR),
    equipment = equipment,
    instructions = instructions?.toList(STEP_SEPARATOR).orEmpty(),
    secondaryMuscleGroups = secondaryMuscleGroups.toList(LIST_SEPARATOR),
    level = TrainingLevel.fromStored(level),
    mechanic = ExerciseMechanic.fromStored(mechanic),
    force = ExerciseForce.fromStored(force),
    category = ExerciseCategory.fromStored(category),
    mediaUrl = mediaUrl,
    thumbUrl = thumbUrl,
    ownerId = ownerId,
)

internal fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    muscleGroups = muscleGroups.joinToString(LIST_SEPARATOR),
    equipment = equipment,
    instructions = instructions.joinToString(STEP_SEPARATOR),
    mediaUrl = mediaUrl,
    thumbUrl = thumbUrl,
    ownerId = ownerId,
    secondaryMuscleGroups = secondaryMuscleGroups.joinToString(LIST_SEPARATOR),
    level = level?.name,
    mechanic = mechanic?.name,
    force = force?.name,
    category = category.name,
)
