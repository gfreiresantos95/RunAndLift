package com.gabrielfreire.runandlift.data.model

/**
 * O sentido do esforço: empurrar, puxar ou sustentar.
 *
 * É o que dá nome à divisão de treino mais comum do mercado — "treino de empurrar" e "treino de
 * puxar" —, e é por isso que o campo vale a coluna: sem ele, montar um dia de puxar significa saber
 * de cor quais dos 577 exercícios de força puxam.
 *
 * Nulo é resposta possível: 29 exercícios da base não declaram.
 */
enum class ExerciseForce {

    /** Supino, desenvolvimento, agachamento — a carga se afasta. */
    PUSH,

    /** Remada, puxada, rosca — a carga se aproxima. */
    PULL,

    /** Prancha, isometria, sustentação — a carga não se move. */
    STATIC,
    ;

    companion object {
        /** Sentido a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): ExerciseForce? = entries.firstOrNull { it.name == value }
    }
}
