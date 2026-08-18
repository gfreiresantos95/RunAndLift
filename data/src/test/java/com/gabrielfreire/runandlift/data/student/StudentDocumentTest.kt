package com.gabrielfreire.runandlift.data.student

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.google.firebase.firestore.FieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * A trava do consentimento de dado de saúde, e o que sobrevive a uma gravação parcial.
 *
 * **É o teste da regra de LGPD do app** (art. 11): peso, altura e lesões só entram no documento
 * depois que o aviso foi aceito. Ela estava dentro do adaptador do Firestore, onde nenhum teste de
 * JVM a alcançava — quebrá-la não daria erro nenhum, gravaria dado sensível sem base legal, e o
 * sintoma só apareceria numa auditoria.
 *
 * O segundo grupo cuida do que o `SetOptions.merge()` transforma em armadilha: campo ausente
 * preserva, e por isso mandar `null` explícito seria apagar dado bom. A exceção é a observação de
 * lesão, onde texto **vazio** é uma decisão ("apaguei") e não uma omissão.
 */
class StudentDocumentTest {

    // -- A trava do consentimento -------------------------------------------------------------

    @Test
    fun `sem consentimento, dado de saude nao entra mesmo vindo preenchido`() {
        val fields = StudentDocument.fields(HEALTH, consented = false)

        assertNull("peso é dado pessoal sensível", fields[StudentDocument.FIELD_WEIGHT])
        assertNull(fields[StudentDocument.FIELD_HEIGHT])
        assertNull(fields[StudentDocument.FIELD_INJURIES])
        assertNull(fields[StudentDocument.FIELD_INJURY_NOTES])
    }

    @Test
    fun `sem consentimento, preferencia de treino continua sendo gravada`() {
        val details = HEALTH.copy(level = TrainingLevel.BEGINNER, goal = TrainingGoal.HYPERTROPHY)
        val fields = StudentDocument.fields(details, consented = false)

        // Dizer que se treina há dois anos não revela condição clínica: travar isto junto seria
        // transformar a recusa do aviso em cadastro impossível.
        assertEquals("BEGINNER", fields[StudentDocument.FIELD_LEVEL])
        assertEquals("HYPERTROPHY", fields[StudentDocument.FIELD_GOAL])
    }

    @Test
    fun `com consentimento, os tres campos de saude entram`() {
        val fields = StudentDocument.fields(HEALTH, consented = true)

        assertEquals(72.5, fields[StudentDocument.FIELD_WEIGHT])
        assertEquals(178, fields[StudentDocument.FIELD_HEIGHT])
        assertEquals(listOf("KNEE", "SHOULDER"), fields[StudentDocument.FIELD_INJURIES])
    }

    @Test
    fun `o aceite e gravado junto do dado que ele autoriza`() {
        val details = HEALTH.copy(healthConsent = HealthDataConsent(HealthDataConsent.CURRENT_VERSION))
        val fields = StudentDocument.fields(details, consented = true)

        @Suppress("UNCHECKED_CAST")
        val consent = fields[StudentDocument.FIELD_HEALTH_CONSENT] as Map<String, Any>

        assertEquals(HealthDataConsent.CURRENT_VERSION, consent[StudentDocument.FIELD_VERSION])
        // Relógio do aparelho, que o titular pode alterar, não prova aceite nenhum (art. 8º, §2º).
        assertTrue(consent[StudentDocument.FIELD_ACCEPTED_AT] is FieldValue)
    }

    @Test
    fun `o aceite sozinho e gravado sem consentimento anterior`() {
        val details = StudentProfileDetails(healthConsent = HealthDataConsent("2026-08-13"))
        val fields = StudentDocument.fields(details, consented = false)

        // É o primeiro passo do fluxo: a tela aceita, e só a chamada seguinte traz peso e altura.
        assertTrue(fields.containsKey(StudentDocument.FIELD_HEALTH_CONSENT))
    }

    // -- Ausente, vazio, e o que cada um faz ---------------------------------------------------

    @Test
    fun `campo nao informado nao vai ao mapa, porque merge apagaria o que ja esta la`() {
        val fields = StudentDocument.fields(StudentProfileDetails(), consented = true)

        assertTrue("gravação parcial só é segura porque o mapa sai vazio", fields.isEmpty())
    }

    @Test
    fun `conjunto vazio de dias e resposta, e e gravado`() {
        val details = StudentProfileDetails(availableDays = emptySet())

        assertEquals(emptyList<Int>(), StudentDocument.fields(details, consented = true)[StudentDocument.FIELD_DAYS])
    }

    @Test
    fun `os dias vao pelo numero ISO, em ordem`() {
        val details = StudentProfileDetails(availableDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.MONDAY))

        assertEquals(listOf(1, 6), StudentDocument.fields(details, consented = true)[StudentDocument.FIELD_DAYS])
    }

    @Test
    fun `lista vazia de lesoes e a resposta nenhuma, e e gravada`() {
        val details = StudentProfileDetails(injuries = emptySet())
        val fields = StudentDocument.fields(details, consented = true)

        // "Declarou não ter" é informação clínica; sem isto, ela ficaria igual a "não respondeu".
        assertEquals(emptyList<String>(), fields[StudentDocument.FIELD_INJURIES])
    }

    @Test
    fun `observacao vazia apaga o texto, em vez de gravar string vazia`() {
        val details = StudentProfileDetails(injuryNotes = "")
        val fields = StudentDocument.fields(details, consented = true)

        // É a única forma de desmarcar "Outra" numa tela de edição e o texto de fato ir embora.
        assertTrue(fields[StudentDocument.FIELD_INJURY_NOTES] is FieldValue)
    }

    @Test
    fun `o campo antigo de texto livre e apagado na primeira gravacao do formato novo`() {
        val details = StudentProfileDetails(injuries = setOf(InjuryArea.KNEE))
        val fields = StudentDocument.fields(details, consented = true)

        // Sobrevivendo em silêncio, ele voltaria a ser lido no dia em que a observação nova fosse
        // esvaziada — o texto que a pessoa apagou reaparecendo sozinho.
        assertTrue(fields[StudentDocument.FIELD_LEGACY_RESTRICTIONS] is FieldValue)
    }

    @Test
    fun `gravacao que nao toca em lesao deixa o campo antigo em paz`() {
        val details = StudentProfileDetails(level = TrainingLevel.BEGINNER)

        assertFalse(
            StudentDocument.fields(details, consented = true).containsKey(StudentDocument.FIELD_LEGACY_RESTRICTIONS),
        )
    }

    // -- Leitura das duas listas ---------------------------------------------------------------

    @Test
    fun `os dias voltam do numero ISO`() {
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY), StudentDocument.days(listOf(1L, 7L)))
    }

    @Test
    fun `dia fora da semana some, e nao derruba a leitura`() {
        assertEquals(setOf(DayOfWeek.FRIDAY), StudentDocument.days(listOf(5L, 9L, "segunda")))
    }

    @Test
    fun `campo de dias ausente vira conjunto vazio`() {
        assertEquals(emptySet<DayOfWeek>(), StudentDocument.days(null))
    }

    @Test
    fun `lesao ausente e nula, e lesao vazia e vazia`() {
        // A distinção inteira do campo: `null` é "não respondeu", vazio é "respondeu que não tem".
        assertNull(StudentDocument.injuries(null))
        assertEquals(emptySet<InjuryArea>(), StudentDocument.injuries(emptyList<String>()))
    }

    @Test
    fun `regiao desconhecida some sem levar as outras junto`() {
        assertEquals(setOf(InjuryArea.KNEE), StudentDocument.injuries(listOf("KNEE", "TENTACULO")))
    }

    // -- O resultado devolvido sem reler --------------------------------------------------------

    @Test
    fun `sem consentimento, o resultado devolvido tambem nao ganha dado de saude`() {
        val merged = StudentDocument.merged(StudentProfile(uid = "aluno-1"), HEALTH, consented = false)

        // Se divergisse do mapa gravado, a tela mostraria um peso que o banco não tem.
        assertNull(merged.weightKg)
        assertNull(merged.injuries)
    }

    @Test
    fun `campo nao informado preserva o que ja estava gravado`() {
        val stored = StudentProfile(uid = "aluno-1", level = TrainingLevel.ADVANCED, weightKg = 80.0)
        val merged = StudentDocument.merged(stored, StudentProfileDetails(goal = TrainingGoal.HEALTH), consented = true)

        assertEquals(TrainingLevel.ADVANCED, merged.level)
        assertEquals(80.0, merged.weightKg)
        assertEquals(TrainingGoal.HEALTH, merged.goal)
    }

    @Test
    fun `observacao esvaziada some do resultado, em vez de voltar a antiga`() {
        val stored = StudentProfile(uid = "aluno-1", injuryNotes = "dói ao levantar acima da cabeça")
        val merged = StudentDocument.merged(stored, StudentProfileDetails(injuryNotes = ""), consented = true)

        // Um `?:` simples devolveria o texto antigo justamente no caso em que ela acabou de apagá-lo.
        assertNull(merged.injuryNotes)
    }

    @Test
    fun `o aceite dado nesta gravacao ja vale no resultado`() {
        val details = StudentProfileDetails(healthConsent = HealthDataConsent(HealthDataConsent.CURRENT_VERSION))
        val merged = StudentDocument.merged(StudentProfile(uid = "aluno-1"), details, consented = true)

        assertTrue(merged.hasHealthConsent)
    }

    private companion object {

        /** Os quatro campos travados pelo consentimento, todos preenchidos e nenhum autorizado. */
        val HEALTH = StudentProfileDetails(
            weightKg = 72.5,
            heightCm = 178,
            injuries = setOf(InjuryArea.SHOULDER, InjuryArea.KNEE),
            injuryNotes = "ombro direito",
        )
    }
}
