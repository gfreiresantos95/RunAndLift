package com.gabrielfreire.runandlift.feature.student.profile

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.InjuryArea
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
        // Sem aceite, peso e lesões **não** contam como faltando: o app não pediu esses dados e
        // não os pode guardar. Cobrá-los seria cobrar uma resposta já recusada.
        assertFalse(missing.measures)
        assertFalse(missing.injuries)
        assertEquals(4, missing.count)
    }

    @Test
    fun `com consentimento, peso e lesoes passam a contar`() {
        val profile = StudentProfile(uid = "u1", healthConsentVersion = HealthDataConsent.CURRENT_VERSION)

        val missing = StudentProfileCompletion.missingIn(profile)

        assertFalse("o aceite existe, então não falta", missing.healthConsent)
        assertTrue(missing.measures)
        assertTrue(missing.injuries)
    }

    @Test
    fun `declarar nenhuma lesao e resposta, e tira o aviso`() {
        val profile = StudentProfile(
            uid = "u1",
            injuries = emptySet(),
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        // É a razão de `injuries` ser um conjunto anulável: sem a diferença entre ausente e vazio,
        // quem não tem lesão nenhuma carregaria para sempre um aviso que não tem como resolver.
        assertFalse(StudentProfileCompletion.missingIn(profile).injuries)
    }

    @Test
    fun `so a observacao ja conta como resposta`() {
        val profile = StudentProfile(
            uid = "u1",
            injuryNotes = "Dói o ombro direito quando levanto acima da cabeça.",
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        // É o caso de quem veio da versão anterior do campo, que era texto livre: o que ela
        // escreveu continua valendo como resposta, sem precisar remarcar nada.
        assertFalse(StudentProfileCompletion.missingIn(profile).injuries)
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
            injuries = setOf(InjuryArea.SHOULDER),
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
