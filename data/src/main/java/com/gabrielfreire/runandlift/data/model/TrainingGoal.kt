package com.gabrielfreire.runandlift.data.model

/**
 * O que o aluno quer do treino.
 *
 * É a primeira coisa que muda uma prescrição — a mesma pessoa treina diferente para ganhar massa e
 * para voltar a subir escada sem cansar —, e é a pergunta que o treinador faria primeiro se
 * estivesse na frente dela.
 *
 * Uma escolha só, e não várias: quem marca tudo não disse nada, e o objetivo principal é o que
 * decide a estrutura do programa. Nuance é conversa para a avaliação.
 *
 * **Não é dado de saúde**: declarar um objetivo não revela condição clínica. [HEALTH] chega perto,
 * e ainda assim é intenção, não diagnóstico.
 */
enum class TrainingGoal {
    /** Ganhar massa muscular. */
    HYPERTROPHY,

    /** Ficar mais forte, sem o tamanho ser o alvo. */
    STRENGTH,

    /** Perder gordura. */
    WEIGHT_LOSS,

    /** Fôlego e resistência para o dia a dia ou para outro esporte. */
    CONDITIONING,

    /** Saúde e disposição, sem meta estética ou de desempenho. */
    HEALTH,
    ;

    companion object {
        /** Objetivo a partir do que está gravado, ou `null` quando o valor não é reconhecido. */
        fun fromStored(value: String?): TrainingGoal? = entries.firstOrNull { it.name == value }
    }
}
