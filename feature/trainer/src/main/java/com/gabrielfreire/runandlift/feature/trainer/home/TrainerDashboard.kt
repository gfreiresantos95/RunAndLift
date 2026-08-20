package com.gabrielfreire.runandlift.feature.trainer.home

/**
 * A semana de trabalho do treinador e quem precisa dele — a parte do painel que ainda é exemplo.
 *
 * Fica separada de [TrainerRoster] de propósito, e a divisão é a linha entre o que é verdade e o
 * que é ilustração. A carteira sai de `links`, que existe; treino entregue, aderência e check-in
 * saem de coleções que o produto não tem. Misturar os dois num objeto só faria a próxima pessoa
 * precisar ler cada campo para saber em qual acreditar.
 *
 * **O que o painel mede veio de olhar o mercado**, e as ferramentas de treinador convergiram para
 * as mesmas quatro perguntas: quanto foi prescrito, quanto foi cumprido, quem respondeu ao contato
 * e — a única que decide o que ele faz hoje — quem está prestes a sumir. As três primeiras são
 * números; a quarta é uma lista de nomes, porque nome é o que se abre e um percentual não é.
 *
 * @param workoutsDelivered treinos prescritos e enviados na semana.
 * @param sessionsLogged sessões que os alunos registraram na semana.
 * @param adherence percentual médio de aderência da carteira, de 0 a 100.
 * @param checkInsAnswered check-ins semanais respondidos, sobre o total enviado.
 * @param checkInsSent quantos check-ins foram enviados na semana.
 * @param attention quem precisa de atenção, do mais grave para o menos.
 */
internal data class TrainerDashboard(
    val workoutsDelivered: Int,
    val sessionsLogged: Int,
    val adherence: Int,
    val checkInsAnswered: Int,
    val checkInsSent: Int,
    val attention: List<AttentionItem>,
) {

    /**
     * Onde a carteira inteira está no semáforo, a partir da aderência média.
     *
     * Os cortes são 80% e 50%, e não uma escala fina: o painel precisa responder "está tudo bem?"
     * de relance, e cinco faixas obrigam a lembrar o que cada uma quer dizer. Oitenta por cento é o
     * limite que a literatura de adesão a exercício usa para "seguiu o programa"; abaixo de metade,
     * o que existe não é um programa em andamento.
     */
    val level: AttentionLevel
        get() = when {
            adherence >= GOOD -> AttentionLevel.OK
            adherence >= POOR -> AttentionLevel.SLIPPING
            else -> AttentionLevel.STOPPED
        }

    /** Quantos alunos aparecem no bloco de atenção — o número que vira a linha de apoio dele. */
    val attentionCount: Int
        get() = attention.size

    companion object {

        /** Aderência a partir da qual o programa está sendo seguido. */
        private const val GOOD = 80

        /** Abaixo disto, menos da metade do que foi prescrito aconteceu. */
        private const val POOR = 50

        /**
         * O exemplo que a home mostra hoje.
         *
         * É uma semana **boa com um problema**, e não uma semana perfeita: 82% de aderência com
         * dois alunos escorregando. Uma semana redonda deixaria o bloco de atenção vazio, que é o
         * único bloco do painel capaz de mudar o que o treinador faz — e um painel se julga pelo que
         * ele mostra quando algo está errado.
         */
        val SAMPLE = TrainerDashboard(
            workoutsDelivered = 9,
            sessionsLogged = 34,
            adherence = 82,
            checkInsAnswered = 7,
            checkInsSent = 11,
            attention = listOf(
                AttentionItem(
                    name = "Rafael Moreira",
                    reason = "Sem registrar treino há 12 dias",
                    level = AttentionLevel.STOPPED,
                ),
                AttentionItem(
                    name = "Juliana Castro",
                    reason = "2 de 4 treinos na semana",
                    level = AttentionLevel.SLIPPING,
                ),
                AttentionItem(
                    name = "Pedro Antunes",
                    reason = "Não respondeu ao último check-in",
                    level = AttentionLevel.SLIPPING,
                ),
            ),
        )
    }
}
