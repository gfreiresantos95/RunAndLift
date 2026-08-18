package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * [ExerciseRemoteDataSource] sobre o Firestore.
 *
 * Aqui ficou só a consulta. O que cada documento vira — e qual deles é descartado — mora em
 * [ExerciseDocument], onde um teste comum o alcança.
 */
internal class FirestoreExerciseRemoteDataSource(private val firestore: FirebaseFirestore) : ExerciseRemoteDataSource {

    override suspend fun fetchGlobalCatalog(): List<Exercise> = firestore.collection(ExerciseDocument.COLLECTION)
        .whereEqualTo(ExerciseDocument.FIELD_OWNER_ID, null)
        .get()
        .await()
        .documents
        .mapNotNull { document ->
            ExerciseDocument.exercise(
                id = document.id,
                name = document.getString(ExerciseDocument.FIELD_NAME),
                muscleGroups = document.get(ExerciseDocument.FIELD_MUSCLE_GROUPS),
                equipment = document.getString(ExerciseDocument.FIELD_EQUIPMENT),
                instructions = document.getString(ExerciseDocument.FIELD_INSTRUCTIONS),
                mediaUrl = document.getString(ExerciseDocument.FIELD_MEDIA_URL),
                thumbUrl = document.getString(ExerciseDocument.FIELD_THUMB_URL),
            )
        }
}
