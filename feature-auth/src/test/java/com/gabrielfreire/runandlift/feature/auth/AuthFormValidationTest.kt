package com.gabrielfreire.runandlift.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Regras do formulário que dá para afirmar sem Android e sem rede.
 *
 * O dia de referência é injetado em todo teste de data: validação de idade que depende do relógio
 * da máquina passa hoje e falha no aniversário de alguém.
 */
class AuthFormValidationTest {

    private val today = LocalDate.of(2026, 8, 8)

    @Test
    fun `nome exige sobrenome, porque o treinador procura numa lista`() {
        assertEquals(NameError.REQUIRED, AuthFormValidation.validateName("   "))
        assertEquals(NameError.INCOMPLETE, AuthFormValidation.validateName("Ana"))
        assertNull(AuthFormValidation.validateName("  Ana Ribeiro  "))
    }

    @Test
    fun `data pela metade e incompleta, nao invalida`() {
        // Quem está no meio da digitação não merece ouvir que a data não existe.
        assertEquals(BirthDateError.REQUIRED, AuthFormValidation.validateBirthDate("", today))
        assertEquals(BirthDateError.INCOMPLETE, AuthFormValidation.validateBirthDate("2105", today))
    }

    @Test
    fun `data que nao existe no calendario e recusada`() {
        assertEquals(BirthDateError.INVALID, AuthFormValidation.validateBirthDate("31021990", today))
        assertEquals(BirthDateError.INVALID, AuthFormValidation.validateBirthDate("00001990", today))
        assertNull("2024 é bissexto", AuthFormValidation.validateBirthDate("29022000", today))
    }

    @Test
    fun `data no futuro e recusada`() {
        assertEquals(BirthDateError.INVALID, AuthFormValidation.validateBirthDate("09082026", today))
    }

    @Test
    fun `idade minima e contada no dia do aniversario`() {
        val birthday = today.minusYears(AuthFormValidation.MIN_AGE_YEARS.toLong())
        val dayBefore = birthday.plusDays(1)

        assertNull("faz a idade mínima hoje", AuthFormValidation.validateBirthDate(birthday.digits(), today))
        assertEquals(
            "faz a idade mínima amanhã",
            BirthDateError.TOO_YOUNG,
            AuthFormValidation.validateBirthDate(dayBefore.digits(), today),
        )
    }

    @Test
    fun `telefone e opcional, mas nao pela metade`() {
        assertNull("vazio é uma resposta válida", AuthFormValidation.validatePhone(""))
        assertEquals(PhoneError.INVALID, AuthFormValidation.validatePhone("1198765"))
        assertEquals(PhoneError.INVALID, AuthFormValidation.validatePhone("119876543210"))
        assertNull(AuthFormValidation.validatePhone("1132654321"))
        assertNull(AuthFormValidation.validatePhone("11987654321"))
    }

    @Test
    fun `parse devolve nulo em vez de estourar`() {
        assertEquals(LocalDate.of(1990, 5, 21), AuthFormValidation.parseBirthDate("21051990"))
        assertNull(AuthFormValidation.parseBirthDate("2105199"))
        assertNull(AuthFormValidation.parseBirthDate("31131990"))
    }

    private fun LocalDate.digits(): String = "%02d%02d%04d".format(dayOfMonth, monthValue, year)
}
