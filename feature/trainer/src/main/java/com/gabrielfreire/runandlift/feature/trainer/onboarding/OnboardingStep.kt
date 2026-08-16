package com.gabrielfreire.runandlift.feature.trainer.onboarding

/**
 * Os passos do passo a passo do treinador, na ordem em que aparecem.
 *
 * A ordem é a da conversa que um aluno teria ao escolher quem o treina: primeiro há quanto tempo a
 * pessoa atua, depois o que ela atende e como, então quando — e só no fim o que só existe para ser
 * publicado.
 *
 * [SHOWCASE_CONSENT] fica no meio de propósito, como o consentimento de saúde do aluno. É a porta
 * dos dois últimos: quem não aceita não vê [BIO] nem [CAPACITY], porque não faz sentido pedir uma
 * apresentação que não vai a lugar nenhum. Pedir o aceite no primeiro passo teria o efeito oposto —
 * ninguém autoriza publicar o próprio nome antes de saber para que serve o aplicativo.
 *
 * **Todo passo pode ser pulado**, inclusive este. O que ficou de fora vira o aviso na home.
 *
 * O registro no CREF não é passo nenhum: ele foi coletado no cadastro, porque sem ele não há
 * treinador — é a única coisa deste perfil que a lei exige (Lei 9.696/1998), e a única que este
 * fluxo não pergunta.
 */
internal enum class OnboardingStep {
    EXPERIENCE,
    SPECIALTIES,
    SERVICE_MODES,
    DAYS,
    SHOWCASE_CONSENT,
    BIO,
    CAPACITY,
    ;

    companion object {
        /** Os passos antes da porta do consentimento — os que todo treinador vê. */
        val ALWAYS_SHOWN = listOf(EXPERIENCE, SPECIALTIES, SERVICE_MODES, DAYS, SHOWCASE_CONSENT)

        /** Os que só existem com autorização para publicar o perfil. */
        val BEHIND_CONSENT = listOf(BIO, CAPACITY)

        /**
         * A sequência para um treinador que autorizou, ou não, a vitrine.
         *
         * O total de passos **muda** quando o aceite é dado, e isso é honesto: o app passa a ter
         * mais o que perguntar porque ganhou permissão para tanto. Fingir sete passos desde o
         * começo prometeria perguntas que talvez nunca fossem feitas.
         */
        fun sequenceFor(showcase: Boolean): List<OnboardingStep> =
            if (showcase) ALWAYS_SHOWN + BEHIND_CONSENT else ALWAYS_SHOWN
    }
}
