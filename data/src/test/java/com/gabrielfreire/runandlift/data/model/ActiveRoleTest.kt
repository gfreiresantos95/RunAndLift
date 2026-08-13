package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * O valor de armazenamento do papel.
 *
 * Parece trivial e não é: [ActiveRole.storageValue] está gravado em `users/{uid}` de toda conta
 * existente, viaja como argumento de rota e é chave de ViewModel. Trocar `"trainer"` por
 * `"TRAINER"` compila, passa em todo teste de tela e **quebra silenciosamente a conta de quem já
 * usa o app** — que volta a ser perguntada sobre o papel que já escolheu.
 */
class ActiveRoleTest {

    @Test
    fun `o valor gravado e minusculo e estavel`() {
        assertEquals("trainer", ActiveRole.TRAINER.storageValue)
        assertEquals("student", ActiveRole.STUDENT.storageValue)
    }

    @Test
    fun `ida e volta preserva o papel`() {
        ActiveRole.entries.forEach {
            assertEquals(it, ActiveRole.fromStorage(it.storageValue))
        }
    }

    @Test
    fun `valor desconhecido vira nulo em vez de exceção`() {
        // Documento corrompido, ou papel de uma versão futura do app: a resposta honesta é "não
        // sei", e quem decide o que fazer com isso é a navegação — não uma exceção na abertura.
        assertNull(ActiveRole.fromStorage(null))
        assertNull(ActiveRole.fromStorage(""))
        assertNull(ActiveRole.fromStorage("TRAINER"))
        assertNull(ActiveRole.fromStorage("coach"))
    }

    @Test
    fun `papeis acumulam sem se excluir`() {
        // Um treinador que também é aluno de outro treinador é caso real e resolvido no modelo,
        // sem segunda conta (§3.2).
        val both = UserRoles(trainer = true, student = true)

        assertEquals(true, both.hasAny)
        assertEquals(true, both.hasBoth)
        assertEquals(false, UserRoles().hasAny)
        assertEquals(false, UserRoles(trainer = true).hasBoth)
    }
}
