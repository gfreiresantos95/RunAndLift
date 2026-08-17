package com.gabrielfreire.runandlift.data.model

import java.time.DayOfWeek

/**
 * Documento `trainerProfiles/{uid}`: o que o aluno precisa saber para escolher e reconhecer quem o
 * treina.
 *
 * É o gêmeo de [StudentProfile] no outro lado do vínculo, e mora fora de `users/{uid}` pela mesma
 * razão: são dois públicos. Identidade é do titular e de mais ninguém; isto aqui é o que o aluno
 * vinculado lê — e, com o aceite da vitrine, o que qualquer pessoa procurando treinador lê.
 *
 * O conteúdo se divide em três naturezas:
 *
 * - **Habilitação** — [cref]. Obrigatório, coletado no cadastro (Lei 9.696/1998), e o único campo
 *   deste documento que **não** se edita aqui: trocar o registro é trocar quem a pessoa é
 *   profissionalmente, e isso é outro fluxo.
 * - **Prática** — [experience], [specialties], [serviceModes], [availableDays]. É o que responde
 *   "esta pessoa atende o que eu preciso, do jeito que eu preciso".
 * - **Vitrine** — [bio], [maxStudents]. Só é gravado depois de [showcaseVersion] existir, porque
 *   só existe para ser publicado. Ver [ShowcaseConsent].
 *
 * Nada além do registro é obrigatório. O passo a passo pergunta, deixa pular, e o que ficou de fora
 * vira o aviso na home — a mesma promessa feita ao aluno.
 *
 * @param cref registro profissional já formatado (`012345-G/SP`), gravado no cadastro. Aparece aqui
 *   porque é deste documento que o aluno o lê; `users/{uid}` é legível só pelo titular.
 * @param availableDays dias em que o treinador **pode** atender. É a restrição de agenda que um
 *   aluno consulta antes de pedir vínculo, e não a agenda cheia dele.
 * @param bio apresentação em texto livre. É a única coisa deste documento escrita nas palavras da
 *   pessoa, e é o que faz um aluno escolher entre dois treinadores com as mesmas especialidades.
 * @param maxStudents quantos alunos o treinador consegue acompanhar ao mesmo tempo. Declarado, não
 *   apurado: serve para ele parar de aparecer na vitrine quando encher, e não para o app policiar.
 * @param showcaseVersion versão do aviso da vitrine que este treinador aceitou, ou `null` se nunca
 *   aceitou. Continua gravada mesmo depois de a vitrine ser desligada — é o registro de que o
 *   aceite existiu.
 * @param showcaseEnabled se o perfil está no ar **agora**. É o campo que a regra do Firestore lê, e
 *   é ele que a retirada do consentimento desliga.
 * @param onboarded se o passo a passo do treinador já aconteceu. **Precisa ser um campo, e não a
 *   existência do documento**: ao contrário de `students/{uid}`, este documento já nasce no
 *   cadastro, com o registro dentro — a existência dele não marca nada.
 */
data class TrainerProfile(
    val uid: String,
    val cref: String? = null,
    val experience: TrainerExperience? = null,
    val specialties: Set<TrainerSpecialty> = emptySet(),
    val serviceModes: Set<ServiceMode> = emptySet(),
    val availableDays: Set<DayOfWeek> = emptySet(),
    val bio: String? = null,
    val maxStudents: Int? = null,
    val showcaseVersion: String? = null,
    val showcaseEnabled: Boolean = false,
    val onboarded: Boolean = false,
) {

    /**
     * Se o treinador autorizou a vitrine na versão vigente do aviso **e** ela está ligada.
     *
     * As duas condições, e não só a versão: quem aceitou e depois se retirou não autorizou nada
     * agora, e é esta propriedade que decide se apresentação e capacidade podem ser gravadas.
     */
    val hasShowcaseConsent: Boolean
        get() = showcaseEnabled && showcaseVersion == ShowcaseConsent.CURRENT_VERSION
}
