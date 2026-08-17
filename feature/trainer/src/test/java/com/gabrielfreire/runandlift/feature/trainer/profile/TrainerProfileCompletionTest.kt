package com.gabrielfreire.runandlift.feature.trainer.profile

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeTrainerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * A régua do aviso de perfil incompleto.
 *
 * Duas regras respondem por quase tudo aqui, e as duas são invisíveis num preview: **o que está
 * atrás do consentimento só é cobrado com o consentimento dado**, e **leitura que falha responde
 * "não falta nada"** — porque um aviso nascido de palpite treina a pessoa a ignorar avisos.
 */
class TrainerProfileCompletionTest {

    @Test
    fun `perfil recem-criado tem tudo a responder, menos o que depende da vitrine`() {
        val missing = TrainerProfileCompletion.missingIn(TrainerProfile(uid = "u1", cref = CREF))

        assertTrue(missing.experience)
        assertTrue(missing.specialties)
        assertTrue(missing.serviceModes)
        assertTrue(missing.availableDays)
        assertTrue(missing.showcase)

        // Sem vitrine não há onde publicar, então cobrá-los seria cobrar uma resposta recusada.
        assertFalse(missing.bio)
        assertFalse(missing.capacity)
        assertEquals(5, missing.count)
    }

    @Test
    fun `perfil completo nao tem aviso`() {
        val missing = TrainerProfileCompletion.missingIn(FakeTrainerRepository.complete())

        assertFalse(missing.any)
        assertEquals(0, missing.count)
    }

    @Test
    fun `com a vitrine aceita, apresentacao e capacidade passam a faltar`() {
        val missing = TrainerProfileCompletion.missingIn(answered().copy(showcaseEnabled = true))

        assertTrue(missing.bio)
        assertTrue(missing.capacity)
        assertFalse(missing.showcase)
        assertEquals(2, missing.count)
    }

    @Test
    fun `vitrine desligada volta a faltar, e o que ela publicava deixa de ser cobrado`() {
        // O caso de quem aceitou e se retirou: a versão fica gravada, mas o aceite não vale agora.
        val missing = TrainerProfileCompletion.missingIn(answered().copy(showcaseEnabled = false))

        assertTrue(missing.showcase)
        assertFalse(missing.bio)
        assertFalse(missing.capacity)
    }

    @Test
    fun `aceite de uma versao antiga do aviso nao vale pela vigente`() {
        val missing = TrainerProfileCompletion.missingIn(
            answered().copy(showcaseEnabled = true, showcaseVersion = "2020-01-01"),
        )

        assertTrue(missing.showcase)
    }

    @Test
    fun `leitura que falha responde que nao falta nada`() = runTest {
        val missing = TrainerProfileCompletion.missing(
            repository = FakeTrainerRepository(failReading = true),
            uid = "u1",
        )

        assertFalse("sem rede e sem cache não dá para afirmar que o perfil está incompleto", missing.any)
    }

    @Test
    fun `documento inexistente tambem responde que nao falta nada`() = runTest {
        // É o caso de quem virou treinador por troca de papel e ainda não tem o documento: o aviso
        // não é o lugar de descobrir isso, e o passo a passo já cuida dele na abertura.
        val missing = TrainerProfileCompletion.missing(repository = FakeTrainerRepository(stored = null), uid = "u1")

        assertFalse(missing.any)
    }

    /** Tudo o que não depende da vitrine, respondido. */
    private fun answered() = TrainerProfile(
        uid = "u1",
        cref = CREF,
        experience = TrainerExperience.UP_TO_TWO_YEARS,
        specialties = setOf(TrainerSpecialty.STRENGTH),
        serviceModes = setOf(ServiceMode.ONLINE),
        availableDays = setOf(DayOfWeek.FRIDAY),
        showcaseVersion = ShowcaseConsent.CURRENT_VERSION,
    )

    private companion object {
        const val CREF = FakeTrainerRepository.CREF
    }
}
