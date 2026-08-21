package com.gabrielfreire.runandlift.data.program

import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.ProgramDay

/**
 * Um dia de treino indo para o Firestore e voltando.
 *
 * Mora à parte de [ProgramDocument] porque **duas coleções gravam exatamente esta forma**: o molde,
 * em `programs`, e a cópia congelada que o aluno recebe, em `assignments`. Duas rotinas para a mesma
 * estrutura divergiriam no dia em que um campo novo entrasse em uma delas — e a divergência
 * apareceria como um treino que perde a observação do treinador ao ser atribuído, que é o tipo de
 * defeito que ninguém liga à causa.
 *
 * A política de leitura é a mesma dos outros adaptadores: **o que estiver estranho some sozinho**,
 * em vez de derrubar o dia ou o programa inteiro. Um exercício sem id numa lista de dez não pode
 * custar os outros nove.
 */
internal object ProgramDays {

    private const val FIELD_LABEL = "label"
    private const val FIELD_FOCUS = "focus"
    private const val FIELD_EXERCISES = "exercises"

    private const val FIELD_EXERCISE_ID = "exerciseId"
    private const val FIELD_EXERCISE_NAME = "exerciseName"
    private const val FIELD_SETS = "sets"
    private const val FIELD_MIN_REPS = "minReps"
    private const val FIELD_MAX_REPS = "maxReps"
    private const val FIELD_LOAD_KG = "loadKg"
    private const val FIELD_REST_SECONDS = "restSeconds"
    private const val FIELD_NOTES = "notes"

    fun toMap(day: ProgramDay): Map<String, Any?> = mapOf(
        FIELD_LABEL to day.label.trim(),
        FIELD_FOCUS to day.focus?.trim()?.takeIf { it.isNotEmpty() },
        FIELD_EXERCISES to day.exercises.map(::exerciseToMap),
    )

    private fun exerciseToMap(exercise: PrescribedExercise): Map<String, Any?> = mapOf(
        FIELD_EXERCISE_ID to exercise.exerciseId,
        FIELD_EXERCISE_NAME to exercise.exerciseName,
        FIELD_SETS to exercise.sets,
        FIELD_MIN_REPS to exercise.minReps,
        FIELD_MAX_REPS to exercise.maxReps,
        FIELD_LOAD_KG to exercise.loadKg,
        FIELD_REST_SECONDS to exercise.restSeconds,
        FIELD_NOTES to exercise.notes?.trim()?.takeIf { it.isNotEmpty() },
    )

    /** Um item da lista de dias virando [ProgramDay], ou `null` quando não tem sequer rótulo. */
    fun day(raw: Any?): ProgramDay? {
        val map = raw as? Map<*, *>
        val label = map?.get(FIELD_LABEL) as? String

        return label?.let {
            ProgramDay(
                label = it,
                focus = map[FIELD_FOCUS] as? String,
                exercises = (map[FIELD_EXERCISES] as? List<*>).orEmpty().mapNotNull(::exercise),
            )
        }
    }

    /**
     * Um item do dia virando [PrescribedExercise].
     *
     * Séries e faixa de repetições caem nos padrões quando vêm ausentes ou zeradas, em vez de
     * descartar o exercício: quem gravou "supino reto" com um número faltando quis o supino, e um
     * exercício a menos no treino é pior do que um número no padrão.
     */
    private fun exercise(raw: Any?): PrescribedExercise? {
        val map = raw as? Map<*, *>
        val id = map?.get(FIELD_EXERCISE_ID) as? String
        if (id == null) return null

        val minReps = map.positiveInt(FIELD_MIN_REPS, PrescribedExercise.DEFAULT_MIN_REPS)
        val maxReps = map.positiveInt(FIELD_MAX_REPS, PrescribedExercise.DEFAULT_MAX_REPS)

        return PrescribedExercise(
            exerciseId = id,
            exerciseName = map[FIELD_EXERCISE_NAME] as? String ?: "",
            sets = map.positiveInt(FIELD_SETS, PrescribedExercise.DEFAULT_SETS),
            minReps = minReps,
            // Faixa invertida é dado corrompido, e não escolha: o maior nunca é menor que o menor.
            maxReps = maxOf(minReps, maxReps),
            loadKg = (map[FIELD_LOAD_KG] as? Number)?.toDouble(),
            restSeconds = (map[FIELD_REST_SECONDS] as? Number)?.toInt(),
            notes = map[FIELD_NOTES] as? String,
        )
    }

    private fun Map<*, *>.positiveInt(field: String, fallback: Int): Int =
        (this[field] as? Number)?.toInt()?.takeIf { it > 0 } ?: fallback
}
