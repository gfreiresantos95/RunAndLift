package com.gabrielfreire.runandlift.data.trainer

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.google.firebase.firestore.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * O que vai para `trainerProfiles/{uid}` e o que a gravação devolve.
 *
 * **É o teste do gate da vitrine**, que é a regra mais cara deste módulo: sem ela, apresentação e
 * capacidade vazariam para um documento que qualquer pessoa autenticada pode ler. A regra mora no
 * repositório justamente para não depender de nenhuma tela lembrar dela — e é aqui que se afirma
 * que ela continua lá.
 *
 * Nenhum teste toca o Firestore: o que se confere é o mapa que seria enviado, e o
 * `FieldValue.delete()` que aparece nele é o sentinela do SDK, não uma chamada de rede.
 */
class TrainerDocumentTest {

    @Test
    fun `campo nao informado nao entra no mapa`() {
        val fields = TrainerDocument.fields(TrainerProfileDetails(), published = true)

        assertTrue("mandar nulo explícito apagaria dado bom", fields.isEmpty())
    }

    @Test
    fun `o que veio preenchido entra, com as listas ordenadas`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(
                experience = TrainerExperience.TWO_TO_FIVE_YEARS,
                specialties = setOf(TrainerSpecialty.STRENGTH, TrainerSpecialty.HYPERTROPHY),
                serviceModes = setOf(ServiceMode.ONLINE),
                availableDays = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY),
            ),
            published = false,
        )

        assertEquals("TWO_TO_FIVE_YEARS", fields[TrainerDocument.FIELD_EXPERIENCE])
        assertEquals(listOf("HYPERTROPHY", "STRENGTH"), fields[TrainerDocument.FIELD_SPECIALTIES])
        assertEquals(listOf("ONLINE"), fields[TrainerDocument.FIELD_MODES])
        // Número ISO e em ordem: é o que ordena sozinho na consulta.
        assertEquals(listOf(1, 3), fields[TrainerDocument.FIELD_DAYS])
    }

    @Test
    fun `conjunto vazio e gravado, porque ali ele e resposta`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(specialties = emptySet(), availableDays = emptySet()),
            published = false,
        )

        assertEquals(emptyList<String>(), fields[TrainerDocument.FIELD_SPECIALTIES])
        assertEquals(emptyList<Int>(), fields[TrainerDocument.FIELD_DAYS])
    }

    @Test
    fun `sem vitrine, apresentacao e capacidade nao entram nem preenchidas`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(bio = "Atendo corredores", maxStudents = 20),
            published = false,
        )

        assertFalse(fields.containsKey(TrainerDocument.FIELD_BIO))
        assertFalse(fields.containsKey(TrainerDocument.FIELD_MAX_STUDENTS))
    }

    @Test
    fun `com vitrine, apresentacao e capacidade entram`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(bio = "Atendo corredores", maxStudents = 20),
            published = true,
        )

        assertEquals("Atendo corredores", fields[TrainerDocument.FIELD_BIO])
        assertEquals(20, fields[TrainerDocument.FIELD_MAX_STUDENTS])
    }

    @Test
    fun `apresentacao vazia apaga o campo`() {
        val fields = TrainerDocument.fields(TrainerProfileDetails(bio = ""), published = true)

        assertEquals(FieldValue.delete(), fields[TrainerDocument.FIELD_BIO])
    }

    @Test
    fun `aceitar a vitrine grava versao, momento e o campo que a regra do Firestore le`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(showcase = ShowcaseConsent(accepted = true)),
            published = true,
        )

        @Suppress("UNCHECKED_CAST")
        val showcase = fields[TrainerDocument.FIELD_SHOWCASE] as Map<String, Any>

        assertEquals(true, showcase[TrainerDocument.FIELD_ENABLED])
        assertEquals(ShowcaseConsent.CURRENT_VERSION, showcase[TrainerDocument.FIELD_VERSION])
        // Carimbado pelo servidor: o relógio do aparelho, que o titular altera, não prova nada.
        assertEquals(FieldValue.serverTimestamp(), showcase[TrainerDocument.FIELD_ACCEPTED_AT])
    }

    @Test
    fun `retirar-se da vitrine desliga sem apagar o registro do aceite`() {
        val fields = TrainerDocument.fields(
            TrainerProfileDetails(showcase = ShowcaseConsent(accepted = false)),
            published = false,
        )

        @Suppress("UNCHECKED_CAST")
        val showcase = fields[TrainerDocument.FIELD_SHOWCASE] as Map<String, Any>

        assertEquals(false, showcase[TrainerDocument.FIELD_ENABLED])
        assertEquals(mapOf(TrainerDocument.FIELD_ENABLED to false), showcase)
    }

    @Test
    fun `a conclusao do passo a passo e carimbada pelo servidor`() {
        val fields = TrainerDocument.fields(TrainerProfileDetails(onboardingDone = true), published = false)

        assertEquals(FieldValue.serverTimestamp(), fields[TrainerDocument.FIELD_ONBOARDED_AT])
    }

    @Test
    fun `a gravacao devolve o que foi escrito somado ao que ja existia`() {
        val stored = TrainerProfile(uid = "u1", cref = CREF, experience = TrainerExperience.UP_TO_TWO_YEARS)

        val merged = stored.mergedWith(
            TrainerProfileDetails(specialties = setOf(TrainerSpecialty.SENIORS)),
            published = false,
        )

        assertEquals(setOf(TrainerSpecialty.SENIORS), merged.specialties)
        assertEquals("campo nulo preserva o que já estava lá", TrainerExperience.UP_TO_TWO_YEARS, merged.experience)
        assertEquals(CREF, merged.cref)
    }

    @Test
    fun `sem vitrine a gravacao devolve o que estava publicado, sem substituir`() {
        val stored = TrainerProfile(uid = "u1", bio = "texto antigo", maxStudents = 30)

        val merged = stored.mergedWith(TrainerProfileDetails(bio = "novo", maxStudents = 10), published = false)

        assertEquals("texto antigo", merged.bio)
        assertEquals(30, merged.maxStudents)
    }

    @Test
    fun `apresentacao apagada volta nula, e nao com o texto antigo`() {
        val stored = TrainerProfile(uid = "u1", bio = "texto antigo")

        val merged = stored.mergedWith(TrainerProfileDetails(bio = ""), published = true)

        assertNull("um elvis simples devolveria o texto que a pessoa acabou de apagar", merged.bio)
    }

    @Test
    fun `retirar-se da vitrine desliga sem perder a versao aceita`() {
        val stored = TrainerProfile(
            uid = "u1",
            showcaseVersion = ShowcaseConsent.CURRENT_VERSION,
            showcaseEnabled = true,
        )

        val merged = stored.mergedWith(
            TrainerProfileDetails(showcase = ShowcaseConsent(accepted = false)),
            published = false,
        )

        assertFalse(merged.showcaseEnabled)
        assertFalse(merged.hasShowcaseConsent)
        assertEquals(ShowcaseConsent.CURRENT_VERSION, merged.showcaseVersion)
    }

    @Test
    fun `o carimbo de conclusao nunca volta atras`() {
        val stored = TrainerProfile(uid = "u1", onboarded = true)

        assertTrue(stored.mergedWith(TrainerProfileDetails(), published = false).onboarded)
        assertTrue(
            TrainerProfile(uid = "u1").mergedWith(TrainerProfileDetails(onboardingDone = true), false).onboarded,
        )
    }

    private companion object {
        const val CREF = "012345-G/SP"
    }
}
