package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.data.model.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As regras da pergunta de lesões — as que a lista de chips não revela ao ser olhada.
 *
 * Três coisas se cobram aqui, e nenhuma delas aparece num preview: que "Nenhuma" e uma região não
 * podem estar marcadas ao mesmo tempo, que **declarar nenhuma é resposta** e chega ao banco como
 * conjunto vazio, e que o que já estava gravado volta com os chips certos acesos.
 */
class InjuryFormRulesTest {

    private val consented = TrainingFormState(healthConsent = true)

    @Test
    fun `marcar uma regiao desmarca nenhuma`() {
        val form = consented.toggleNoInjuries().toggleInjury(InjuryArea.KNEE)

        // "Não tenho lesão nenhuma, e o joelho" não é resposta — é o treinador escolhendo em qual
        // metade acreditar.
        assertFalse(form.noInjuries)
        assertEquals(setOf(InjuryArea.KNEE), form.injuries)
    }

    @Test
    fun `marcar nenhuma limpa as regioes e a observacao`() {
        val form = consented
            .toggleInjury(InjuryArea.SHOULDER)
            .toggleOtherInjury()
            .copy(injuryNotes = "Dói ao levantar acima da cabeça")
            .toggleNoInjuries()

        assertTrue(form.noInjuries)
        assertEquals(emptySet<InjuryArea>(), form.injuries)
        // Inclusive o texto: quem acabou de dizer que não tem nada não segue com a observação da
        // lesão que tinha escrito antes.
        assertFalse(form.otherInjury)
        assertEquals("", form.injuryNotes)
    }

    @Test
    fun `desmarcar outra apaga o que estava escrito`() {
        val form = consented
            .toggleOtherInjury()
            .copy(injuryNotes = "Dói ao levantar acima da cabeça")
            .toggleOtherInjury()

        // Desmarcar é uma decisão. Guardar o texto escondido faria ele reaparecer ao remarcar, e
        // pior, ser gravado sem estar visível.
        assertEquals("", form.injuryNotes)
    }

    @Test
    fun `outra marcada e vazia ainda nao e resposta`() {
        val form = consented.toggleOtherInjury()

        // Um chip aceso e um campo em branco é uma resposta começada. Contá-la calaria o aviso da
        // home sem que nada tivesse sido dito.
        assertFalse(form.injuriesAnswered)
    }

    @Test
    fun `declarar nenhuma chega ao banco como conjunto vazio`() {
        val details = consented.toggleNoInjuries().toDetails(includeDays = false, consentJustGiven = false)

        // Vazio e não nulo: é a diferença entre "respondi que não tenho" e "não respondi", e é ela
        // que decide se o aviso da home some.
        assertEquals(emptySet<InjuryArea>(), details.injuries)
    }

    @Test
    fun `pergunta nao respondida nao vai ao banco`() {
        val details = consented.toDetails(includeDays = false, consentJustGiven = false)

        assertNull("nulo é 'não mexa nisto', e é o que preserva o que já estava lá", details.injuries)
        assertNull(details.injuryNotes)
    }

    @Test
    fun `retirar o consentimento apaga as lesoes`() {
        val form = MutableStateFlow(
            TrainingFormState(
                healthConsent = true,
                injuries = setOf(InjuryArea.KNEE),
                otherInjury = true,
                injuryNotes = "Dói ao agachar",
            ),
        )

        trainingFormActions(form).onHealthConsentChange(false)

        // Dado sensível não fica em memória à espera de uma autorização que foi retirada — a mesma
        // regra que já valia para peso e altura.
        assertEquals(emptySet<InjuryArea>(), form.value.injuries)
        assertEquals("", form.value.injuryNotes)
        assertFalse(form.value.otherInjury)
    }

    @Test
    fun `conjunto vazio gravado volta com nenhuma marcada`() {
        val profile = StudentProfile(
            uid = "u1",
            injuries = emptySet(),
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        val form = TrainingFormState().prefilledFrom(profile)

        // Quem respondeu "não tenho" reencontra a própria resposta, e não o formulário em branco de
        // quem nunca respondeu.
        assertTrue(form.noInjuries)
    }

    @Test
    fun `texto livre da versao anterior volta no campo Outra`() {
        val profile = StudentProfile(
            uid = "u1",
            injuryNotes = "Dói o ombro direito quando levanto acima da cabeça.",
            healthConsentVersion = HealthDataConsent.CURRENT_VERSION,
        )

        val form = TrainingFormState().prefilledFrom(profile)

        // É o caminho de quem escreveu antes de a lista existir: o chip vem aceso e o texto
        // preenchido, em vez de o que ela escreveu simplesmente sumir porque o formato mudou.
        assertTrue(form.otherInjury)
        assertEquals("Dói o ombro direito quando levanto acima da cabeça.", form.injuryNotes)
        assertFalse("com texto, 'Nenhuma' seria contraditória", form.noInjuries)
    }
}
