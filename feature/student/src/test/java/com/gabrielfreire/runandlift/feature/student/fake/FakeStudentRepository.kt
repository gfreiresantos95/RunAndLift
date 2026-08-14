package com.gabrielfreire.runandlift.feature.student.fake

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.student.StudentRepository
import java.time.DayOfWeek

/**
 * [StudentRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Guarda [lastDetails] porque as regras que importam são sobre **o que chega ao banco**: o que foi
 * pulado não é escrito, os dias vazios só vão quando houve resposta, e o consentimento só é enviado
 * quando acabou de ser dado.
 *
 * Reproduz a regra de consentimento do repositório real — sem aceite, os campos de saúde não são
 * gravados —, porque é ela que os testes de tela precisam ver acontecendo de ponta a ponta.
 *
 * @param stored o que já existe no documento. `null` é o caso do primeiro acesso, e é o que faz o
 *   app abrir no onboarding.
 * @param failReading leitura que não responde — sem rede e sem cache.
 * @param failWriting gravação que não completa, com a conta já existindo.
 */
internal class FakeStudentRepository(
    private val stored: StudentProfile? = null,
    private val failReading: Boolean = false,
    private val failWriting: Boolean = false,
) : StudentRepository {

    var lastDetails: StudentProfileDetails? = null
        private set

    var saveCount: Int = 0
        private set

    override suspend fun profile(uid: String): StudentProfile? {
        if (failReading) error("sem rede e sem cache")

        return stored
    }

    override suspend fun save(uid: String, details: StudentProfileDetails): StudentProfile {
        if (failWriting) error("gravação não completou")

        saveCount++
        lastDetails = details

        val consented = details.healthConsent != null || stored?.hasHealthConsent == true

        return StudentProfile(
            uid = uid,
            level = details.level ?: stored?.level,
            goal = details.goal ?: stored?.goal,
            availableDays = details.availableDays ?: stored?.availableDays.orEmpty(),
            weightKg = details.weightKg.takeIf { consented } ?: stored?.weightKg,
            heightCm = details.heightCm.takeIf { consented } ?: stored?.heightCm,
            restrictions = details.restrictions.takeIf { consented } ?: stored?.restrictions,
            healthConsentVersion = details.healthConsent?.version ?: stored?.healthConsentVersion,
        )
    }

    companion object {
        /** Um perfil que já respondeu tudo, incluindo o aceite vigente. */
        fun complete(uid: String = "u1") = StudentProfile(
            uid = uid,
            level = TrainingLevel.INTERMEDIATE,
            goal = TrainingGoal.HYPERTROPHY,
            availableDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            weightKg = 72.5,
            heightCm = 175,
            restrictions = "Ombro direito",
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )
    }
}
