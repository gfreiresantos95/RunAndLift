package com.gabrielfreire.runandlift.feature.trainer.profile

import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository

/**
 * O que falta no perfil profissional, para o aviso da home e para a tela de edição.
 *
 * Existe porque o passo a passo **deixa pular todos os passos**: terminar não significa ter
 * respondido. O que ficou de fora vira um aviso na home, que é a segunda chance — e não um
 * bloqueio, porque nada disto impede o treinador de prescrever para quem já é aluno dele.
 *
 * É o gêmeo de `StudentProfileCompletion`, e segue a mesma regra que importa: **leitura que falha
 * responde "não falta nada"**. Sem rede e sem cache não dá para afirmar que o perfil está
 * incompleto, e um aviso nascido de palpite treina a pessoa a ignorar avisos.
 */
internal object TrainerProfileCompletion {

    /**
     * O que falta em `trainerProfiles/{uid}`.
     *
     * Custo declarado: **0 leitura** com o documento em cache, 1 quando não está. Roda na abertura
     * da home, que é caminho quente — por isso uma leitura só, e nenhuma consulta.
     */
    suspend fun missing(repository: TrainerRepository, uid: String): MissingTrainerData {
        val profile = runCatching { repository.profile(uid) }.getOrNull() ?: return MissingTrainerData()

        return missingIn(profile)
    }

    /**
     * A mesma régua, sobre um perfil já em mãos.
     *
     * Separada para a tela de edição não pagar uma segunda leitura do documento que ela acabou de
     * carregar — e para a regra ser testável sem repositório nenhum.
     */
    fun missingIn(profile: TrainerProfile): MissingTrainerData {
        val published = profile.hasShowcaseConsent

        return MissingTrainerData(
            experience = profile.experience == null,
            specialties = profile.specialties.isEmpty(),
            serviceModes = profile.serviceModes.isEmpty(),
            availableDays = profile.availableDays.isEmpty(),
            // Os dois só existem com a vitrine no ar: um perfil fora dela não tem onde publicá-los.
            bio = published && profile.bio.isNullOrBlank(),
            capacity = published && profile.maxStudents == null,
            showcase = !published,
        )
    }
}
