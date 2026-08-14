package com.gabrielfreire.runandlift.feature.student.onboarding

/**
 * Os passos do onboarding do aluno, na ordem em que aparecem.
 *
 * A ordem é a da conversa que um treinador teria: primeiro o que se quer e de onde se parte, depois
 * a agenda, e só então o corpo — que é a parte que exige autorização para ser guardada.
 *
 * [HEALTH_CONSENT] fica no meio de propósito. É a porta dos dois últimos: quem não aceita não vê
 * [MEASURES] nem [INJURIES], porque não faz sentido perguntar o que não se pode guardar. Pedir
 * o aceite no primeiro passo teria o efeito oposto — ninguém autoriza dado sensível antes de saber
 * para que serve o aplicativo.
 *
 * **Todo passo pode ser pulado**, inclusive este. O que ficou de fora vira o aviso na home.
 */
internal enum class OnboardingStep {
    LEVEL,
    GOAL,
    DAYS,
    HEALTH_CONSENT,
    MEASURES,
    INJURIES,
    ;

    companion object {
        /** Os passos antes da porta do consentimento — os que todo aluno vê. */
        val ALWAYS_SHOWN = listOf(LEVEL, GOAL, DAYS, HEALTH_CONSENT)

        /** Os que só existem com autorização para guardar dado de saúde. */
        val BEHIND_CONSENT = listOf(MEASURES, INJURIES)

        /**
         * A sequência para um aluno que autorizou, ou não, o tratamento de dado de saúde.
         *
         * O total de passos **muda** quando o aceite é dado, e isso é honesto: o app passa a ter
         * mais o que perguntar porque ganhou permissão para tanto. Fingir seis passos desde o
         * começo prometeria perguntas que talvez nunca fossem feitas.
         */
        fun sequenceFor(healthConsent: Boolean): List<OnboardingStep> =
            if (healthConsent) ALWAYS_SHOWN + BEHIND_CONSENT else ALWAYS_SHOWN
    }
}
