package com.gabrielfreire.runandlift.data.model

/**
 * Em que pé está a prescrição de um aluno.
 *
 * Duas situações e não cinco, ao contrário de [LinkStatus]: o vínculo é uma relação que se negocia —
 * pede, aceita, pausa —, e a prescrição é um ato do profissional. Ela vale ou não vale mais.
 */
enum class AssignmentStatus {

    /** O treino que o aluno está fazendo agora. */
    ACTIVE,

    /** Prescrição encerrada. O aluno deixa de ver o treino, e o treinador pode atribuir outro. */
    ENDED,
    ;

    /** O que vai gravado no documento. **Minúsculo**, como em `LinkStatus`, porque é o que as regras comparam. */
    val stored: String get() = name.lowercase()

    companion object {
        /**
         * Situação a partir do que está gravado, ou [ACTIVE] quando o valor não é reconhecido.
         *
         * O padrão é ativo porque um documento de atribuição só existe se alguém o criou: tratar o
         * desconhecido como encerrado esconderia o treino de um aluno por causa de um campo torto.
         */
        fun fromStored(value: String?): AssignmentStatus = entries.firstOrNull { it.stored == value } ?: ACTIVE
    }
}
