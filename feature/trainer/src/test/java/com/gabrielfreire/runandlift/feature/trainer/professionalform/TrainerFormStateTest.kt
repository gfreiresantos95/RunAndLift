package com.gabrielfreire.runandlift.feature.trainer.professionalform

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.feature.trainer.validation.TrainerFormValidation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * As regras do formulário que nenhuma tela deve reimplementar.
 *
 * A que mais importa é a última: **retirar o aceite da vitrine limpa o que existia para ser
 * publicado**. Uma autorização retirada com a apresentação ainda em memória é a autorização valendo
 * na prática — o próximo toque em salvar a republicaria.
 */
class TrainerFormStateTest {

    @Test
    fun `marcar e desmarcar uma especialidade`() {
        val marked = TrainerFormState().toggleSpecialty(TrainerSpecialty.SENIORS)

        assertEquals(setOf(TrainerSpecialty.SENIORS), marked.specialties)
        assertTrue(marked.toggleSpecialty(TrainerSpecialty.SENIORS).specialties.isEmpty())
    }

    @Test
    fun `as escolhas multiplas sao independentes entre si`() {
        val form = TrainerFormState()
            .toggleServiceMode(ServiceMode.IN_PERSON)
            .toggleServiceMode(ServiceMode.ONLINE)
            .toggleDay(DayOfWeek.SATURDAY)

        // Presencial e online juntos são o que "híbrido" quer dizer aqui — não há terceira opção.
        assertEquals(setOf(ServiceMode.IN_PERSON, ServiceMode.ONLINE), form.serviceModes)
        assertEquals(setOf(DayOfWeek.SATURDAY), form.availableDays)
    }

    @Test
    fun `o contador da apresentacao conta o que ainda cabe`() {
        assertEquals(TrainerFormValidation.MAX_BIO_LENGTH, TrainerFormState().bioRemaining)
        assertEquals(TrainerFormValidation.MAX_BIO_LENGTH - 3, TrainerFormState(bio = "abc").bioRemaining)
    }

    @Test
    fun `a apresentacao e cortada no limite em vez de recusada`() {
        val state = MutableStateFlow(TrainerFormState())

        trainerFormActions(state).onBioChange("a".repeat(TrainerFormValidation.MAX_BIO_LENGTH + 50))

        assertEquals(TrainerFormValidation.MAX_BIO_LENGTH, state.value.bio.length)
        assertEquals(0, state.value.bioRemaining)
    }

    @Test
    fun `a capacidade so aceita digito`() {
        val state = MutableStateFlow(TrainerFormState())

        trainerFormActions(state).onMaxStudentsChange("2a0 alunos")

        assertEquals("20", state.value.maxStudents)
    }

    @Test
    fun `retirar o aceite da vitrine limpa o que existia para ser publicado`() {
        val state = MutableStateFlow(TrainerFormState())
        val actions = trainerFormActions(state)

        actions.onShowcaseChange(true)
        actions.onBioChange("Atendo corredores")
        actions.onMaxStudentsChange("20")
        actions.onSpecialtyToggle(TrainerSpecialty.RUNNING)

        actions.onShowcaseChange(false)

        assertFalse(state.value.showcase)
        assertEquals("", state.value.bio)
        assertEquals("", state.value.maxStudents)
        // O que não é vitrine fica: o aluno vinculado lê isto de qualquer forma.
        assertEquals(setOf(TrainerSpecialty.RUNNING), state.value.specialties)
    }

    @Test
    fun `a validacao confere so a capacidade`() {
        val invalid = TrainerFormState(maxStudents = "0").validated()

        assertFalse(invalid.isValid)
        assertNull(TrainerFormState(bio = "qualquer coisa").validated().maxStudentsError)
        assertTrue(TrainerFormState().validated().isValid)
    }
}
