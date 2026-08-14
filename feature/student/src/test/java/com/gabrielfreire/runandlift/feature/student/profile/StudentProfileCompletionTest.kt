package com.gabrielfreire.runandlift.feature.student.profile

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.fake.FakeStudentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * O que alimenta o aviso da home.
 *
 * A regra que mais importa está no último teste: **leitura que falha responde "não falta nada"**.
 * Um aviso nascido de palpite treina a pessoa a ignorar avisos, e o preço de deixar passar é a
 * próxima abertura com rede, que cobra de novo.
 */
class StudentProfileCompletionTest {

    @Test
    fun `perfil vazio tem tudo a responder, menos o que depende de consentimento`() {
        val missing = StudentProfileCompletion.missingIn(StudentProfile(uid = "u1"))

        assertTrue(missing.level)
        assertTrue(missing.goal)
        assertTrue(missing.availableDays)
        assertTrue(missing.healthConsent)
        // Sem aceite, peso e restrições **não** contam como faltando: o app não pediu esses dados e
        // não os pode guardar. Cobrá-los seria cobrar uma resposta já recusada.
        assertFalse(missing.measures)
        assertFalse(missing.restrictions)
        assertEquals(4, missing.count)
    }

    @Test
    fun `com consentimento, peso e restricoes passam a contar`() {
        val profile = StudentProfile(uid = "u1", healthConsentVersion = HealthDataConsent.CURRENT_VERSION)

        val missing = StudentProfileCompletion.missingIn(profile)

        assertFalse("o aceite existe, então não falta", missing.healthConsent)
        assertTrue(missing.measures)
        assertTrue(missing.restrictions)
    }

    @Test
    fun `peso sem altura ainda conta como pergunta pela metade`() {
        val profile = StudentProfile(
            uid = "u1",
            weightKg = 72.5,
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        assertTrue(StudentProfileCompletion.missingIn(profile).measures)
    }

    @Test
    fun `aceite de versao antiga nao vale pela vigente`() {
        val profile = StudentProfile(uid = "u1", healthConsentVersion = "2020-01-01")

        // Mudou o texto que descreve o uso do dado de saúde, o aceite anterior deixa de cobri-lo.
        assertTrue(StudentProfileCompletion.missingIn(profile).healthConsent)
    }

    @Test
    fun `perfil completo nao falta nada`() {
        val profile = StudentProfile(
            uid = "u1",
            level = TrainingLevel.INTERMEDIATE,
            goal = TrainingGoal.HYPERTROPHY,
            availableDays = setOf(DayOfWeek.MONDAY),
            weightKg = 72.5,
            heightCm = 175,
            restrictions = "Ombro direito",
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        assertFalse(StudentProfileCompletion.missingIn(profile).any)
    }

    @Test
    fun `leitura que falha responde que nao falta nada`() = runTest {
        val missing = StudentProfileCompletion.missing(FakeStudentRepository(failReading = true), uid = "u1")

        assertFalse("aviso nascido de palpite treina a pessoa a ignorar avisos", missing.any)
    }

    @Test
    fun `documento inexistente responde que nao falta nada`() = runTest {
        // Quem ainda não passou pelo onboarding não recebe aviso: ele abre no passo a passo, e
        // cobrar o que nunca foi perguntado seria cobrar duas vezes.
        val missing = StudentProfileCompletion.missing(FakeStudentRepository(stored = null), uid = "u1")

        assertFalse(missing.any)
    }
}
