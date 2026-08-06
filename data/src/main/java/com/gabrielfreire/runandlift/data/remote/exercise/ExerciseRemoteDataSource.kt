package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Leitura do catálogo global no Firestore.
 *
 * Contrato de custo: **uma varredura da coleção `exercises`, e só quando a versão mudou**. Quem
 * decide se vale a pena chamar é o repositório — esta classe não tem política, só transporte.
 */
internal interface ExerciseRemoteDataSource {
    suspend fun fetchGlobalCatalog(): List<Exercise>
}

internal class FirestoreExerciseRemoteDataSource(private val firestore: FirebaseFirestore) : ExerciseRemoteDataSource {

    override suspend fun fetchGlobalCatalog(): List<Exercise> = firestore.collection(COLLECTION)
        .whereEqualTo(FIELD_OWNER_ID, null)
        .get()
        .await()
        .documents
        .mapNotNull { document ->
            val name = document.getString(FIELD_NAME) ?: return@mapNotNull null
            Exercise(
                id = document.id,
                name = name,
                muscleGroups = (document.get(FIELD_MUSCLE_GROUPS) as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty(),
                equipment = document.getString(FIELD_EQUIPMENT),
                instructions = document.getString(FIELD_INSTRUCTIONS),
                mediaUrl = document.getString(FIELD_MEDIA_URL),
                thumbUrl = document.getString(FIELD_THUMB_URL),
                ownerId = null,
            )
        }

    private companion object {
        const val COLLECTION = "exercises"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_NAME = "name"
        const val FIELD_MUSCLE_GROUPS = "muscleGroups"
        const val FIELD_EQUIPMENT = "equipment"
        const val FIELD_INSTRUCTIONS = "instructions"
        const val FIELD_MEDIA_URL = "mediaUrl"
        const val FIELD_THUMB_URL = "thumbUrl"
    }
}
