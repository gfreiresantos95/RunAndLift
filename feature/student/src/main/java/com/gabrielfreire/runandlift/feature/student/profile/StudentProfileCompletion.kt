package com.gabrielfreire.runandlift.feature.student.profile

import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.student.StudentRepository

/**
 * O que falta no perfil de treino, para o aviso da home e para a tela de edição.
 *
 * Existe porque o onboarding **deixa pular todos os passos**: terminar não significa ter
 * respondido. O que ficou de fora vira um aviso na home, que é a segunda chance — e não um bloqueio,
 * porque nada disto impede o aluno de treinar.
 *
 * É o gêmeo de `ProfileCompletion` do `:feature:auth`, e segue a mesma regra que importa: **leitura
 * que falha responde "não falta nada"**. Sem rede e sem cache não dá para afirmar que o perfil está
 * incompleto, e um aviso nascido de palpite treina a pessoa a ignorar avisos.
 */
internal object StudentProfileCompletion {

    /**
     * O que falta em `students/{uid}`.
     *
     * Custo declarado: **0 leitura** com o documento em cache, 1 quando não está. Roda na abertura
     * da home, que é caminho quente — por isso uma leitura só, e nenhuma consulta.
     */
    suspend fun missing(repository: StudentRepository, uid: String): MissingStudentData {
        val profile = runCatching { repository.profile(uid) }.getOrNull() ?: return MissingStudentData()

        return missingIn(profile)
    }

    /**
     * A mesma régua, sobre um perfil já em mãos.
     *
     * Separada para a tela de edição não pagar uma segunda leitura do documento que ela acabou de
     * carregar — e para a regra ser testável sem repositório nenhum.
     */
    fun missingIn(profile: StudentProfile): MissingStudentData {
        val consented = profile.hasHealthConsent

        return MissingStudentData(
            level = profile.level == null,
            goal = profile.goal == null,
            availableDays = profile.availableDays.isEmpty(),
            // Os dois juntos: peso sem altura não é meia resposta útil, é uma pergunta pela metade.
            measures = consented && (profile.weightKg == null || profile.heightCm == null),
            // Respondida de qualquer forma conta como respondida, inclusive "não tenho nenhuma" —
            // que é o caso mais comum e o único que, sem esta distinção, ficaria preso no aviso
            // para sempre.
            injuries = consented && !profile.hasAnsweredInjuries,
            healthConsent = !consented,
        )
    }
}
