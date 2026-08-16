package com.gabrielfreire.runandlift.data.model

/**
 * Há quanto tempo o treinador atua, em faixas.
 *
 * É o gêmeo de [TrainingLevel] do outro lado do vínculo, e existe pelo mesmo motivo: o aluno que
 * escolhe um treinador precisa de uma âncora grossa, não de um currículo. Quatro faixas e não um
 * campo de anos — "7" e "8" não distinguem ninguém, e um número exato envelhece sozinho no
 * documento, ao contrário de uma faixa que a pessoa reconfirma quando editar o perfil.
 *
 * **Não é o mesmo que o registro no CREF.** O registro diz que a pessoa *pode* prescrever, e é
 * obrigatório (Lei 9.696/1998); isto diz há quanto tempo ela o faz, e é opcional como todo o resto
 * do perfil profissional.
 *
 * O rótulo de cada faixa é resolvido em `:feature:trainer` — este módulo não tem recurso de string
 * e não escolhe idioma.
 */
enum class TrainerExperience {
    /** Está começando: até dois anos desde a formação ou desde a volta à atuação. */
    UP_TO_TWO_YEARS,

    /** De dois a cinco anos atendendo. */
    TWO_TO_FIVE_YEARS,

    /** De cinco a dez anos atendendo. */
    FIVE_TO_TEN_YEARS,

    /** Mais de dez anos atendendo. */
    OVER_TEN_YEARS,
    ;

    companion object {
        /** Faixa a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): TrainerExperience? = entries.firstOrNull { it.name == value }
    }
}
