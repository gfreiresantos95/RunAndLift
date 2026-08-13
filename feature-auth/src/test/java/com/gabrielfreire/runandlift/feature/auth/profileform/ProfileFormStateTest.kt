package com.gabrielfreire.runandlift.feature.auth.profileform

import com.gabrielfreire.runandlift.feature.auth.validation.CrefError
import com.gabrielfreire.runandlift.feature.auth.validation.NameError
import com.gabrielfreire.runandlift.feature.auth.validation.PhoneError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A régua do formulário de perfil.
 *
 * Testada direto, e não só através dos ViewModels: é aqui que mora **toda** a diferença entre o
 * cadastro de aluno e o de treinador, e a mesma função serve às duas telas que a usam. Um erro
 * aqui aparece como "o app pediu CREF para um aluno", que é caro e silencioso.
 */
class ProfileFormStateTest {

    private val filled = ProfileFormState(
        name = "Ana Ribeiro",
        birthDate = "21051990",
        phone = "11987654321",
        acceptedTerms = true,
    )

    @Test
    fun `formulario vazio nao e valido antes de ser conferido`() {
        // `isValid` só significa alguma coisa depois de `validated` — antes, tudo parece válido,
        // porque nenhum erro foi preenchido ainda.
        assertTrue("antes de conferir, o vazio passa", ProfileFormState().isValid)
        assertFalse("depois de conferir, não passa", ProfileFormState().validated(isTrainer = false).isValid)
    }

    @Test
    fun `aluno nao precisa de celular nem de registro`() {
        val validated = filled.copy(phone = "").validated(isTrainer = false)

        assertNull(validated.phoneError)
        assertNull(validated.crefError)
        assertTrue(validated.isValid)
    }

    @Test
    fun `treinador precisa dos dois`() {
        val validated = filled.copy(phone = "").validated(isTrainer = true)

        assertEquals(PhoneError.REQUIRED, validated.phoneError)
        assertEquals(CrefError.REQUIRED, validated.crefError)
        assertFalse(validated.isValid)
    }

    @Test
    fun `papel desconhecido vale a regua do aluno`() {
        // Cadastro alcançado sem passar pelas boas-vindas: barrar alguém por um campo que a tela
        // nem mostrou seria um beco sem saída.
        val validated = filled.copy(phone = "").validated(isTrainer = false)

        assertTrue(validated.isValid)
    }

    @Test
    fun `askName false nao cobra o nome`() {
        // É a tela de conclusão: o nome veio do provedor e não tem campo onde consertá-lo.
        val validated = ProfileFormState(birthDate = "21051990", acceptedTerms = true)
            .validated(isTrainer = false, askName = false)

        assertNull(validated.nameError)
        assertTrue(validated.isValid)
    }

    @Test
    fun `askName true cobra o nome`() {
        val validated = ProfileFormState(birthDate = "21051990", acceptedTerms = true)
            .validated(isTrainer = false)

        assertEquals(NameError.REQUIRED, validated.nameError)
        assertFalse(validated.isValid)
    }

    @Test
    fun `askConsent false nao repete o pedido a quem ja consentiu`() {
        val validated = filled.copy(acceptedTerms = false)
            .validated(isTrainer = false, askConsent = false)

        assertFalse(validated.termsMissing)
        assertTrue(validated.isValid)
    }

    @Test
    fun `conferir preenche todos os erros de uma vez`() {
        val validated = ProfileFormState().validated(isTrainer = true)

        // Formulário que revela um problema por envio faz a pessoa tentar quatro vezes para
        // descobrir quatro coisas.
        val errors = listOfNotNull(
            validated.nameError,
            validated.birthDateError,
            validated.phoneError,
            validated.crefError,
        )

        assertEquals(4, errors.size)
        assertTrue(validated.termsMissing)
    }

    @Test
    fun `treinador completo passa`() {
        val validated = filled.copy(cref = "012345GSP").validated(isTrainer = true)

        assertTrue(validated.isValid)
    }
}
