package com.gabrielfreire.runandlift.feature.auth.validation

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
    fun `telefone do treinador e obrigatorio, porque ele e o canal`() {
        assertEquals(PhoneError.REQUIRED, AuthFormValidation.validatePhone("", required = true))
        assertNull(AuthFormValidation.validatePhone("11987654321", required = true))
    }

    @Test
    fun `cref e guardado sem separador e gravado com ele`() {
        // O campo mascarado entrega só o conteúdo; a pontuação da carteira entra na gravação.
        assertEquals("012345-G/SP", AuthFormValidation.formatCref("012345GSP"))
        assertNull("incompleto não vira registro pela metade", AuthFormValidation.formatCref("012345G"))
        assertEquals("012345GSP", AuthFormValidation.crefContent("012345-G/SP"))
    }

    @Test
    fun `cref incompleto ou com estado inexistente nao passa`() {
        assertEquals(CrefError.REQUIRED, AuthFormValidation.validateCref(""))
        assertEquals("faltou o estado", CrefError.INVALID, AuthFormValidation.validateCref("012345G"))
        assertEquals("XX não é unidade da federação", CrefError.INVALID, AuthFormValidation.validateCref("012345GXX"))
        assertNull(AuthFormValidation.validateCref("012345GSP"))
    }

    @Test
    fun `cref aceita as duas categorias que prescrevem`() {
        assertNull("graduado", AuthFormValidation.validateCref("012345GSP"))
        assertNull("provisionado", AuthFormValidation.validateCref("012345PSP"))
    }

    @Test
    fun `categoria fora de G e P e recusada com erro proprio`() {
        // A máscara garante *uma letra* naquela posição, não *qual* letra: `012345ESP` tinha o
        // formato certo e entrava como registro válido.
        assertEquals(CrefError.INVALID_CATEGORY, AuthFormValidation.validateCref("012345ESP"))
        assertEquals(CrefError.INVALID_CATEGORY, AuthFormValidation.validateCref("012345XSP"))
    }

    @Test
    fun `categoria invalida nao vira gravacao`() {
        // O formato canônico passa pela mesma régua, então um registro recusado na tela também não
        // chega ao banco por outro caminho.
        assertNull(AuthFormValidation.formatCref("012345ESP"))
    }

    @Test
    fun `erro de categoria e distinguido do erro de estado`() {
        // As duas mensagens são diferentes de propósito: quem errou a categoria acertou o número e
        // a sigla, e mandá-lo "conferir o registro" o faz procurar erro onde não há.
        assertEquals(CrefError.INVALID_CATEGORY, AuthFormValidation.validateCref("012345ESP"))
        assertEquals(CrefError.INVALID, AuthFormValidation.validateCref("012345GXX"))
    }

    @Test
    fun `email exige dominio com ponto e topo de duas letras`() {
        assertEquals(EmailError.REQUIRED, AuthFormValidation.validateEmail("  "))
        assertEquals("domínio sem ponto", EmailError.INVALID, AuthFormValidation.validateEmail("ana@exemplo"))
        assertEquals("topo de uma letra", EmailError.INVALID, AuthFormValidation.validateEmail("ana@exemplo.c"))
        assertEquals("arroba dobrada", EmailError.INVALID, AuthFormValidation.validateEmail("ana@@exemplo.com"))
        assertEquals("espaço no meio", EmailError.INVALID, AuthFormValidation.validateEmail("an a@exemplo.com"))
        assertNull(AuthFormValidation.validateEmail("ana.ribeiro+treino@exemplo.com.br"))
        assertNull("espaço nas pontas é do teclado, não do endereço", AuthFormValidation.validateEmail(" ana@x.co "))
    }

    @Test
    fun `senha curta e recusada antes de ir a rede`() {
        val short = "a".repeat(AuthFormValidation.MIN_PASSWORD_LENGTH - 1)

        assertEquals(
            PasswordError.TOO_SHORT,
            AuthFormValidation.validatePassword(short, requireMinLength = true),
        )
        assertNull(AuthFormValidation.validatePassword(short + "a", requireMinLength = true))
        // Ao entrar a regra não vale: quem tem senha antiga mais curta não pode ser barrado por
        // uma exigência que passou a existir depois da conta dele.
        assertNull(AuthFormValidation.validatePassword(short, requireMinLength = false))
    }

    @Test
    fun `parse devolve nulo em vez de estourar`() {
        assertEquals(LocalDate.of(1990, 5, 21), AuthFormValidation.parseBirthDate("21051990"))
        assertNull(AuthFormValidation.parseBirthDate("2105199"))
        assertNull(AuthFormValidation.parseBirthDate("31131990"))
    }

    private fun LocalDate.digits(): String = "%02d%02d%04d".format(dayOfMonth, monthValue, year)
}
