package com.gabrielfreire.runandlift.feature.trainer.students

import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.fake.FakeLinkRepository.Companion.link
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Como uma lista só de vínculos vira três blocos ordenados.
 *
 * É a decisão principal da tela e ela não vem do banco: o repositório devolve tudo junto, em ordem
 * de documento. **Um pedido novo no meio de trinta nomes conhecidos é um pedido que fica dias sem
 * resposta** — é isso que os dois primeiros testes protegem.
 */
class StudentsUiStateTest {

    @Test
    fun `quem espera resposta fica no proprio bloco`() {
        val state = StudentsUiState(
            loading = false,
            links = listOf(
                link(LinkStatus.ACTIVE, name = "Ana"),
                link(LinkStatus.REQUESTED, name = "Bruno"),
                link(LinkStatus.INVITED, name = "Carla"),
            ),
        )

        assertEquals(listOf("Bruno", "Carla"), state.pending.map { it.studentName })
        assertEquals(listOf("Ana"), state.current.map { it.studentName })
    }

    @Test
    fun `ativo vem antes de pausado, porque pausado e excecao`() {
        val state = StudentsUiState(
            loading = false,
            links = listOf(
                link(LinkStatus.PAUSED, name = "Ana"),
                link(LinkStatus.ACTIVE, name = "Zulmira"),
            ),
        )

        assertEquals(listOf("Zulmira", "Ana"), state.current.map { it.studentName })
    }

    @Test
    fun `encerrados continuam na tela, no fim`() {
        val state = StudentsUiState(
            loading = false,
            links = listOf(link(LinkStatus.ENDED, name = "Diego"), link(LinkStatus.ACTIVE, name = "Ana")),
        )

        // Aluno que some de uma lista sem explicação vira dúvida: "cancelei sem querer?"
        assertEquals(listOf("Diego"), state.past.map { it.studentName })
        assertFalse(state.isEmpty)
    }

    @Test
    fun `dentro do bloco a ordem e alfabetica`() {
        val state = StudentsUiState(
            loading = false,
            links = listOf(
                link(LinkStatus.ACTIVE, name = "carla"),
                link(LinkStatus.ACTIVE, name = "Ana"),
                link(LinkStatus.ACTIVE, name = "Bruno"),
            ),
        )

        // Minúscula não vai para o fim da lista: quem digitou o nome sem maiúscula continua entre os
        // outros, e não depois de todos eles.
        assertEquals(listOf("Ana", "Bruno", "carla"), state.current.map { it.studentName })
    }

    @Test
    fun `nome vazio vai para o fim, e nao para o comeco`() {
        val state = StudentsUiState(
            loading = false,
            links = listOf(link(LinkStatus.ACTIVE, name = ""), link(LinkStatus.ACTIVE, name = "Ana")),
        )

        // Ordenar por texto vazio poria as linhas menos reconhecíveis na primeira posição.
        assertEquals(listOf("Ana", ""), state.current.map { it.studentName })
    }

    @Test
    fun `carteira sem nada e vazia, mas falha nao e vazia`() {
        assertTrue(StudentsUiState(loading = false).isEmpty)

        val failed = StudentsUiState(loading = false, failed = true)

        // A tela usa as duas respostas para escolher entre "convide alguém" e "tente de novo".
        assertTrue(failed.isEmpty)
        assertTrue(failed.failed)
    }

    @Test
    fun `so a linha em gravacao fica em espera`() {
        val ana = link(LinkStatus.REQUESTED, name = "Ana")
        val bruno = link(LinkStatus.ACTIVE, name = "Bruno")
        val state = StudentsUiState(loading = false, links = listOf(ana, bruno), updating = ana.studentId)

        assertTrue(state.isUpdating(ana))
        assertFalse("uma gravação não pode congelar a carteira inteira", state.isUpdating(bruno))
    }
}
