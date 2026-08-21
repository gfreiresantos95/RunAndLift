package com.gabrielfreire.runandlift.data.model

/**
 * Se o exercício move uma articulação ou várias.
 *
 * É a informação que decide a **ordem dos exercícios dentro de um treino**: composto primeiro,
 * enquanto há força disponível, isolado depois. Um treinador faz essa conta toda vez que monta um
 * dia, e é barato o app carregar o dado em vez de deixá-lo lembrar de cor.
 *
 * Nulo é resposta possível — 87 exercícios da base não declaram, e inventar seria pior do que a
 * ausência.
 */
enum class ExerciseMechanic {

    /** Mais de uma articulação: agachamento, supino, remada. */
    COMPOUND,

    /** Uma articulação só: rosca direta, cadeira extensora, elevação lateral. */
    ISOLATION,
    ;

    companion object {
        /** Mecânica a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): ExerciseMechanic? = entries.firstOrNull { it.name == value }
    }
}
