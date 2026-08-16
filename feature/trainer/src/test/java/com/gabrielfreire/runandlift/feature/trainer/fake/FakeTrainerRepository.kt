package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import java.time.DayOfWeek

/**
 * [TrainerRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Guarda [lastDetails] porque as regras que importam são sobre **o que chega ao banco**: o que foi
 * pulado não é escrito, o conjunto vazio só vai quando a pergunta estava na tela, e a decisão sobre
 * a vitrine só é enviada quando mudou.
 *
 * Reproduz a regra da vitrine do repositório real — sem aceite, apresentação e capacidade não são
 * gravadas —, porque é ela que os testes de tela precisam ver acontecendo de ponta a ponta.
 *
 * @param stored o que já existe no documento. O padrão é um documento que **existe** com o registro
 *   no CREF e nada mais: é o estado real de quem acabou de se cadastrar, e é ele que mostra por que
 *   a existência do documento não serve de marca de "o passo a passo aconteceu".
 * @param failReading leitura que não responde — sem rede e sem cache.
 * @param failWriting gravação que não completa, com a conta já existindo.
 */
internal class FakeTrainerRepository(
    private val stored: TrainerProfile? = TrainerProfile(uid = "u1", cref = CREF),
    private val failReading: Boolean = false,
    private val failWriting: Boolean = false,
) : TrainerRepository {

    var lastDetails: TrainerProfileDetails? = null
        private set

    var saveCount: Int = 0
        private set

    override suspend fun profile(uid: String): TrainerProfile? {
        if (failReading) error("sem rede e sem cache")

        return stored
    }

    override suspend fun save(uid: String, details: TrainerProfileDetails): TrainerProfile {
        if (failWriting) error("gravação não completou")

        saveCount++
        lastDetails = details

        // Em variáveis porque os campos vêm de outro módulo, onde o compilador não faz smart cast.
        val showcase = details.showcase
        val bio = details.bio
        val published = showcase?.accepted ?: (stored?.hasShowcaseConsent == true)

        return TrainerProfile(
            uid = uid,
            cref = stored?.cref,
            experience = details.experience ?: stored?.experience,
            specialties = details.specialties ?: stored?.specialties.orEmpty(),
            serviceModes = details.serviceModes ?: stored?.serviceModes.orEmpty(),
            availableDays = details.availableDays ?: stored?.availableDays.orEmpty(),
            // Espelha o repositório real: ausente preserva, vazio apaga.
            bio = if (published && bio != null) bio.takeIf { it.isNotEmpty() } else stored?.bio,
            maxStudents = details.maxStudents.takeIf { published } ?: stored?.maxStudents,
            showcaseVersion = showcase?.version?.takeIf { showcase.accepted } ?: stored?.showcaseVersion,
            showcaseEnabled = showcase?.accepted ?: (stored?.showcaseEnabled == true),
            onboarded = stored?.onboarded == true || details.onboardingDone,
        )
    }

    companion object {
        const val CREF = "012345-G/SP"

        /** Um perfil que já respondeu tudo, com a vitrine no ar e o passo a passo concluído. */
        fun complete(uid: String = "u1") = TrainerProfile(
            uid = uid,
            cref = CREF,
            experience = TrainerExperience.TWO_TO_FIVE_YEARS,
            specialties = setOf(TrainerSpecialty.HYPERTROPHY),
            serviceModes = setOf(ServiceMode.IN_PERSON),
            availableDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            bio = "Atendo em estúdio, com avaliação a cada oito semanas.",
            maxStudents = 20,
            showcaseVersion = ShowcaseConsent.CURRENT_VERSION,
            showcaseEnabled = true,
            onboarded = true,
        )
    }
}
