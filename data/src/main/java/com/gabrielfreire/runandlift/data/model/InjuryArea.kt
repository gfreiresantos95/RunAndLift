package com.gabrielfreire.runandlift.data.model

/**
 * Região do corpo com lesão ou limitação.
 *
 * **Por região, e não por diagnóstico**, que é como a anamnese de mercado pergunta e — mais
 * importante — é o que muda uma prescrição. O PAR-Q+ formula exatamente assim ("bone, joint, or soft
 * tissue problem — for example, back, knee or hip"), e as fichas de academia perguntam por dor na
 * coluna e nas articulações. A razão é prática: quem escreve o treino não precisa saber se é
 * tendinite ou bursite para tirar o desenvolvimento acima da cabeça — precisa saber que é o ombro.
 * Diagnóstico é conversa da avaliação, com quem tem competência para lê-lo.
 *
 * A ordem é **da cabeça aos pés**, que é como um exame físico corre e como a pessoa varre o próprio
 * corpo ao lembrar. Ordenar por frequência de lesão faria a lista parecer arbitrária para quem a lê.
 *
 * As nove cobrem o mapa articular que a musculação carrega. O que não couber nelas tem o campo de
 * texto ao lado — ver `StudentProfile.injuryNotes`.
 *
 * **É dado de saúde** (LGPD art. 5º, II), como peso e altura: só é gravado depois do consentimento
 * próprio, e a regra vive no repositório, não na tela.
 */
enum class InjuryArea {
    NECK,
    SHOULDER,
    ELBOW,
    WRIST_HAND,
    UPPER_BACK,
    LOWER_BACK,
    HIP,
    KNEE,
    ANKLE_FOOT,
    ;

    companion object {
        /** Região a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): InjuryArea? = entries.firstOrNull { it.name == value }
    }
}
