package com.gabrielfreire.runandlift.data.model

/**
 * Os cinco estados do vínculo entre treinador e aluno.
 *
 * A ordem é a da vida do vínculo: alguém propõe ([INVITED] ou [REQUESTED]), a contraparte confirma
 * ([ACTIVE]), e a partir daí ele só pode ser suspenso ([PAUSED]) ou encerrado ([ENDED]).
 *
 * **Não existe estado "recusado"**, e isso é decisão: recusar é encerrar antes de começar, e um
 * sexto valor obrigaria toda tela e toda regra a tratar dois finais que se comportam igual. O que
 * distingue os dois casos é o histórico dentro do documento, não o estado atual.
 *
 * [ENDED] nunca vira apagar. As Security Rules recusam `delete` em `links/{id}`: o aluno mantém
 * acesso permanente ao próprio histórico e o treinador mantém o do período em que atendeu, que é
 * exigência de responsabilidade técnica (Res. CONFEF 542/2024).
 *
 * @param stored o que vai ao banco — **minúsculo, e não [name]**. É o literal que as Security Rules
 *   comparam (`status == 'active'`), então o texto gravado aqui e o escrito na regra são a mesma
 *   decisão em dois arquivos: mudar um sem o outro tranca todo mundo do lado de fora.
 */
enum class LinkStatus(val stored: String) {

    /** O treinador convidou e espera o aluno aceitar. */
    INVITED("invited"),

    /** O aluno pediu e espera o treinador aceitar. É o estado em que nasce o vínculo por código. */
    REQUESTED("requested"),

    /** Vale agora: é o único estado em que o treinador alcança os dados do aluno. */
    ACTIVE("active"),

    /** Suspenso pelos dois lados, e reversível — férias, lesão, uma pausa de pagamento. */
    PAUSED("paused"),

    /** Encerrado. Estado final: de [ENDED] não se volta, cria-se um vínculo novo. */
    ENDED("ended"),
    ;

    /** Se está esperando a confirmação de alguém — o que a tela mostra separado da carteira. */
    val isPending: Boolean
        get() = this == INVITED || this == REQUESTED

    companion object {

        /**
         * O valor gravado de volta ao enum, ou `null` se o texto não for nenhum deles.
         *
         * Nulo em vez de exceção pela mesma razão dos outros enums do módulo: documento com campo
         * estranho — escrito por uma versão futura, ou corrompido — não pode derrubar a lista de
         * alunos de quem está tentando trabalhar.
         */
        fun fromStored(value: String?): LinkStatus? = entries.firstOrNull { it.stored == value }
    }
}
