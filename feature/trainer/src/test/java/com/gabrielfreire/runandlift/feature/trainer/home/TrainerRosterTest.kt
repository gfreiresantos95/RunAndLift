package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As contagens da carteira — a única parte do painel do treinador que vem do banco.
 *
 * O que se afirma aqui não é aritmética: é **quem entra em qual conta**. Pausado contar no tamanho
 * da carteira e encerrado não contar é decisão de produto, e é o tipo de decisão que uma refatoração
 * distraída inverte sem que nenhuma tela reclame.
 */
class TrainerRosterTest {

    @Test
    fun `ativo e o que responde quantos alunos eu tenho`() {
        val roster = roster(LinkStatus.ACTIVE, LinkStatus.ACTIVE, LinkStatus.PAUSED, LinkStatus.ENDED)

        assertEquals(2, roster.active)
    }

    @Test
    fun `pedido e convite contam juntos, porque os dois esperam alguem`() {
        val roster = roster(LinkStatus.REQUESTED, LinkStatus.INVITED, LinkStatus.ACTIVE)

        assertEquals(2, roster.pending)
    }

    @Test
    fun `pausado ocupa vaga e por isso entra no tamanho da carteira`() {
        val roster = roster(LinkStatus.ACTIVE, LinkStatus.ACTIVE, LinkStatus.PAUSED)

        assertEquals(2, roster.active)
        assertEquals(1, roster.paused)
        assertEquals("quem pausou volta, e a vaga continua ocupada", 3, roster.size)
    }

    @Test
    fun `encerrado fica fora do tamanho, e continua contado a parte`() {
        val roster = roster(LinkStatus.ACTIVE, LinkStatus.ENDED, LinkStatus.ENDED)

        assertEquals(1, roster.size)
        assertEquals("o histórico é responsabilidade técnica, não lixo", 2, roster.ended)
    }

    @Test
    fun `carteira sem vinculo nenhum e vazia`() {
        assertTrue(TrainerRoster(links = emptyList()).isEmpty)
    }

    @Test
    fun `quem encerrou todos nao e quem nunca teve nenhum`() {
        val roster = roster(LinkStatus.ENDED)

        assertEquals(0, roster.size)
        assertFalse("o painel tem frases diferentes para os dois casos", roster.isEmpty)
    }

    private fun roster(vararg statuses: LinkStatus) = TrainerRoster(
        links = statuses.mapIndexed { index, status ->
            Link(
                trainerId = "treinador",
                studentId = "aluno-$index",
                status = status,
                origin = LinkOrigin.INVITE_CODE,
                studentName = "Aluno $index",
            )
        },
    )
}
