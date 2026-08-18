package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.Exercise

/**
 * O documento do catálogo virando [Exercise], ou sendo descartado.
 *
 * Mora fora de [FirestoreExerciseRemoteDataSource] pela razão de sempre — lá está a consulta, aqui
 * a decisão —, e a decisão aqui é uma só, dita em duas linhas: **exercício sem nome não entra, e
 * qualquer outro campo estranho não impede o resto de entrar**. Um registro mal formado no catálogo
 * global não pode derrubar a sincronização inteira e deixar o app sem exercício nenhum; e um
 * exercício sem nome não pode ser mostrado numa lista, porque não há o que mostrar.
 */
internal object ExerciseDocument {

    const val COLLECTION = "exercises"

    const val FIELD_OWNER_ID = "ownerId"
    const val FIELD_NAME = "name"
    const val FIELD_MUSCLE_GROUPS = "muscleGroups"
    const val FIELD_EQUIPMENT = "equipment"
    const val FIELD_INSTRUCTIONS = "instructions"
    const val FIELD_MEDIA_URL = "mediaUrl"
    const val FIELD_THUMB_URL = "thumbUrl"

    /**
     * @param name `null` descarta o documento — é o único campo sem o qual não há exercício.
     * @param muscleGroups o que não for texto some da lista, em vez de derrubá-la.
     */
    fun exercise(
        id: String,
        name: String?,
        muscleGroups: Any? = null,
        equipment: String? = null,
        instructions: String? = null,
        mediaUrl: String? = null,
        thumbUrl: String? = null,
    ): Exercise? {
        if (name == null) return null

        return Exercise(
            id = id,
            name = name,
            muscleGroups = (muscleGroups as? List<*>)?.filterIsInstance<String>().orEmpty(),
            equipment = equipment,
            instructions = instructions,
            mediaUrl = mediaUrl,
            thumbUrl = thumbUrl,
            // O catálogo global é o que esta consulta traz, e nele ninguém é dono de nada.
            ownerId = null,
        )
    }
}
