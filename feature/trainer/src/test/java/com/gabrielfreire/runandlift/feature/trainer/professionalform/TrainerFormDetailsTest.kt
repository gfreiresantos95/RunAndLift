package com.gabrielfreire.runandlift.feature.trainer.professionalform

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeTrainerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * A tradução entre o formulário e o que a camada de dados grava, nos dois sentidos.
 *
 * É onde mora a regra que decide se uma resposta some: **conjunto vazio só é gravado onde ele é
 * escolha**. No passo a passo, vazio quer dizer "pulei", e gravá-lo apagaria o que já estivesse no
 * documento; na edição, vazio é uma decisão de quem está olhando a pergunta.
 */
class TrainerFormDetailsTest {

    @Test
    fun `no passo a passo, lista vazia nao e gravada`() {
        val details = TrainerFormState().toDetails(answered = false, showcaseChanged = false)

        assertNull(details.specialties)
        assertNull(details.serviceModes)
        assertNull(details.availableDays)
    }

    @Test
    fun `no passo a passo, lista marcada e gravada`() {
        val details = TrainerFormState(specialties = setOf(TrainerSpecialty.RUNNING))
            .toDetails(answered = false, showcaseChanged = false)

        assertEquals(setOf(TrainerSpecialty.RUNNING), details.specialties)
    }

    @Test
    fun `na edicao, lista vazia e gravada como escolha`() {
        val details = TrainerFormState().toDetails(answered = true, showcaseChanged = false)

        assertEquals(emptySet<TrainerSpecialty>(), details.specialties)
        assertEquals(emptySet<ServiceMode>(), details.serviceModes)
        assertEquals(emptySet<DayOfWeek>(), details.availableDays)
    }

    @Test
    fun `sem vitrine, a apresentacao nao e enviada — nem para apagar`() {
        val details = TrainerFormState(showcase = false, bio = "sobra do formulário")
            .toDetails(answered = true, showcaseChanged = false)

        assertNull("retirar-se da vitrine não é pedido de exclusão do que já foi publicado", details.bio)
    }

    @Test
    fun `com vitrine, apresentacao vazia e enviada para apagar o campo`() {
        val details = TrainerFormState(showcase = true, bio = "")
            .toDetails(answered = true, showcaseChanged = false)

        assertEquals("", details.bio)
    }

    @Test
    fun `a decisao sobre a vitrine so e enviada quando muda`() {
        val form = TrainerFormState(showcase = true)

        assertNull(form.toDetails(answered = true, showcaseChanged = false).showcase)
        assertEquals(
            ShowcaseConsent(accepted = true),
            form.toDetails(answered = true, showcaseChanged = true).showcase,
        )
    }

    @Test
    fun `desmarcar a vitrine envia a retirada`() {
        val details = TrainerFormState(showcase = false).toDetails(answered = true, showcaseChanged = true)

        assertEquals(ShowcaseConsent(accepted = false), details.showcase)
    }

    @Test
    fun `o passo a passo nunca marca a conclusao por acidente`() {
        assertFalse(TrainerFormState().toDetails(answered = true, showcaseChanged = false).onboardingDone)
    }

    @Test
    fun `o que esta gravado volta para o formulario`() {
        val form = TrainerFormState().prefilledFrom(FakeTrainerRepository.complete())

        assertEquals(TrainerExperience.TWO_TO_FIVE_YEARS, form.experience)
        assertEquals(setOf(TrainerSpecialty.HYPERTROPHY), form.specialties)
        assertEquals(setOf(ServiceMode.IN_PERSON), form.serviceModes)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), form.availableDays)
        assertEquals("20", form.maxStudents)
        assertTrue(form.showcase)
    }

    @Test
    fun `perfil inexistente devolve o formulario em branco`() {
        val form = TrainerFormState().prefilledFrom(profile = null)

        assertNull(form.experience)
        assertTrue(form.specialties.isEmpty())
        assertEquals("", form.bio)
        assertFalse(form.showcase)
    }
}
