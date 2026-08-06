package com.gabrielfreire.runandlift.data.local.exercise

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gabrielfreire.runandlift.data.model.Exercise

/**
 * Linha da tabela de exercícios. `internal` porque tipo do Room não sai de `:data`.
 *
 * [muscleGroups] é gravado como texto separado por vírgula em vez de tabela associada. A alternativa
 * normalizada custaria um JOIN e uma migração a mais para resolver um problema que este dado não
 * tem: a lista é curta, imutável na prática e nunca é consultada isoladamente.
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
)

private const val MUSCLE_GROUP_SEPARATOR = ","

internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    muscleGroups = muscleGroups.split(MUSCLE_GROUP_SEPARATOR).filter { it.isNotBlank() },
    equipment = equipment,
    instructions = instructions,
    mediaUrl = mediaUrl,
    thumbUrl = thumbUrl,
    ownerId = ownerId,
)

internal fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    muscleGroups = muscleGroups.joinToString(MUSCLE_GROUP_SEPARATOR),
    equipment = equipment,
    instructions = instructions,
    mediaUrl = mediaUrl,
    thumbUrl = thumbUrl,
    ownerId = ownerId,
)
