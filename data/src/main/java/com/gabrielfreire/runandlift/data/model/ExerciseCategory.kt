package com.gabrielfreire.runandlift.data.model

/**
 * A que família um exercício pertence.
 *
 * Existe porque o catálogo importado **não é só musculação**: dos 868 exercícios, 577 são de força e
 * o resto é alongamento, pliometria, powerlifting, levantamento olímpico, strongman e cardio. Sem
 * este campo, um treinador procurando "abdominal" recebe alongamento de lombar no meio da lista.
 *
 * Ficou como enum, e não como texto livre, porque é conjunto fechado que a tela filtra — a mesma
 * regra que separa [TrainingLevel] de `muscleGroups`, que é texto livre e é buscado por `LIKE`.
 */
enum class ExerciseCategory {

    /** Musculação. É a maior parte do catálogo e o padrão de todo filtro. */
    STRENGTH,

    /** Alongamento e mobilidade. */
    STRETCHING,

    /** Saltos e arremessos — treino de potência. */
    PLYOMETRICS,

    /** Agachamento, supino e terra sob a ótica da competição. */
    POWERLIFTING,

    /** Arranco e arremesso. */
    OLYMPIC_WEIGHTLIFTING,

    /** Carregamentos, viradas de pneu e afins. */
    STRONGMAN,

    /** Esteira, bicicleta, corda. */
    CARDIO,
    ;

    companion object {

        /**
         * Categoria a partir do que está gravado, ou [STRENGTH] quando o valor não é reconhecido.
         *
         * **O padrão é musculação e não `null`** de propósito: o campo alimenta um filtro, e um
         * exercício sem categoria sumiria de todas as listas por não casar com nenhuma opção. Cair
         * na família mais provável é o erro barato; desaparecer é o caro.
         */
        fun fromStored(value: String?): ExerciseCategory = entries.firstOrNull { it.name == value } ?: STRENGTH
    }
}
