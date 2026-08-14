package com.gabrielfreire.runandlift.data.model

/**
 * Experiência do aluno com musculação, como **ele** a descreve.
 *
 * Três faixas e não cinco: quem responde isso não tem como se auto-classificar com precisão, e uma
 * escala fina só produziria respostas que o treinador teria de refazer na avaliação. O que o
 * treinador precisa saber daqui é grosso — se pode prescrever um agachamento livre já na primeira
 * semana ou se a conversa começa antes disso.
 *
 * **Não é dado de saúde**, e é por isso que mora fora do bloco que exige consentimento próprio:
 * dizer que se treina há dois anos não revela condição física nem histórico clínico.
 *
 * O rótulo de cada faixa é resolvido em `:feature:student`, não aqui — este módulo não tem
 * recurso de string e não escolhe idioma.
 */
enum class TrainingLevel {
    /** Nunca treinou, ou parou há tanto tempo que volta do começo. */
    BEGINNER,

    /** Treina com alguma regularidade e conhece os movimentos principais. */
    INTERMEDIATE,

    /** Treina há anos, sem interrupção longa, e acompanha as próprias cargas. */
    ADVANCED,
    ;

    companion object {
        /** Faixa a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): TrainingLevel? = entries.firstOrNull { it.name == value }
    }
}
