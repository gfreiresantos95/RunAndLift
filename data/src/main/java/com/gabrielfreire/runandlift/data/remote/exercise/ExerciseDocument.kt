package com.gabrielfreire.runandlift.data.remote.exercise

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.ExerciseCategory
import com.gabrielfreire.runandlift.data.model.ExerciseForce
import com.gabrielfreire.runandlift.data.model.ExerciseMechanic
import com.gabrielfreire.runandlift.data.model.TrainingLevel

/**
 * O documento do catálogo virando [Exercise], ou sendo descartado.
 *
 * Mora fora de [FirestoreExerciseRemoteDataSource] pela razão de sempre — lá está a consulta, aqui
 * a decisão —, e a decisão aqui é uma só, dita em duas linhas: **exercício sem nome não entra, e
 * qualquer outro campo estranho não impede o resto de entrar**. Um registro mal formado no catálogo
 * global não pode derrubar a sincronização inteira e deixar o app sem exercício nenhum; e um
 * exercício sem nome não pode ser mostrado numa lista, porque não há o que mostrar.
 *
 * **Os nomes de campo aqui são um contrato com `tools/catalog/build-catalog.js`.** Renomear um lado
 * sem o outro não quebra nada de forma visível: a leitura devolve `null` para campo inexistente, e o
 * catálogo inteiro passa a chegar mudo naquele campo. É o tipo de erro que só aparece quando alguém
 * procura por um filtro e não acha nada.
 */
internal object ExerciseDocument {

    const val COLLECTION = "exercises"

    const val FIELD_OWNER_ID = "ownerId"
    const val FIELD_NAME = "name"
    const val FIELD_MUSCLE_GROUPS = "muscleGroups"
    const val FIELD_SECONDARY_MUSCLE_GROUPS = "secondaryMuscleGroups"
    const val FIELD_EQUIPMENT = "equipment"
    const val FIELD_INSTRUCTIONS = "instructions"
    const val FIELD_LEVEL = "level"
    const val FIELD_MECHANIC = "mechanic"
    const val FIELD_FORCE = "force"
    const val FIELD_CATEGORY = "category"
    const val FIELD_MEDIA_URL = "mediaUrl"
    const val FIELD_THUMB_URL = "thumbUrl"

    /**
     * @param name `null` descarta o documento — é o único campo sem o qual não há exercício.
     * @param muscleGroups o que não for texto some da lista, em vez de derrubá-la. Vale igual para
     *   [secondaryMuscleGroups] e [instructions].
     */
    @Suppress("LongParameterList")
    fun exercise(id: String, name: String?, fields: Fields = Fields()): Exercise? {
        if (name == null) return null

        return Exercise(
            id = id,
            name = name,
            muscleGroups = fields.muscleGroups.asTextList(),
            equipment = fields.equipment,
            instructions = fields.instructions.asTextList(),
            secondaryMuscleGroups = fields.secondaryMuscleGroups.asTextList(),
            level = TrainingLevel.fromStored(fields.level),
            mechanic = ExerciseMechanic.fromStored(fields.mechanic),
            force = ExerciseForce.fromStored(fields.force),
            category = ExerciseCategory.fromStored(fields.category),
            mediaUrl = fields.mediaUrl,
            thumbUrl = fields.thumbUrl,
            // O catálogo global é o que esta consulta traz, e nele ninguém é dono de nada.
            ownerId = null,
        )
    }

    /**
     * Os campos opcionais do documento, agrupados.
     *
     * Existem como um objeto e não como doze parâmetros porque doze parâmetros posicionais de tipos
     * quase todos iguais é onde um `equipment` trocado por um `level` passa despercebido pelo
     * compilador e pela revisão.
     */
    data class Fields(
        val muscleGroups: Any? = null,
        val secondaryMuscleGroups: Any? = null,
        val equipment: String? = null,
        val instructions: Any? = null,
        val level: String? = null,
        val mechanic: String? = null,
        val force: String? = null,
        val category: String? = null,
        val mediaUrl: String? = null,
        val thumbUrl: String? = null,
    )

    private fun Any?.asTextList(): List<String> = (this as? List<*>)?.filterIsInstance<String>().orEmpty()
}
