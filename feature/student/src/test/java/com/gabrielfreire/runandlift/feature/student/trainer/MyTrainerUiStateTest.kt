package com.gabrielfreire.runandlift.feature.student.trainer

import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.student.fake.FakeLinkRepository.Companion.link
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Qual dos vínculos da lista é "o meu treinador".
 *
 * Um aluno tem um treinador, mas a lista pode trazer mais de um documento — um encerrado do ano
 * passado, um pedido feito ontem. Escolher errado aqui é mostrar como treinador atual quem parou de
 * treinar a pessoa há meses.
 */
class MyTrainerUiStateTest {

    @Test
    fun `encerrado nao e o treinador atual`() {
        val state = MyTrainerUiState(loading = false, links = listOf(link(LinkStatus.ENDED)))

        assertNull(state.current)
        assertEquals(1, state.past.size)
        assertTrue("sem vínculo vigente, o campo de código volta", state.canEnterCode)
    }

    @Test
    fun `pedido pendente ja e o vinculo da tela`() {
        val state = MyTrainerUiState(loading = false, links = listOf(link(LinkStatus.REQUESTED)))

        // Se não fosse, a tela ofereceria digitar outro código enquanto o primeiro pedido espera —
        // e a pessoa mandaria dois pedidos achando que o primeiro falhou.
        assertEquals(LinkStatus.REQUESTED, state.current?.status)
        assertFalse(state.canEnterCode)
    }

    @Test
    fun `ativo ganha de pendente quando os dois existem`() {
        val state = MyTrainerUiState(
            loading = false,
            links = listOf(
                link(LinkStatus.REQUESTED, trainerId = "treinador-2"),
                link(LinkStatus.ACTIVE, trainerId = "treinador-1"),
            ),
        )

        assertEquals("treinador-1", state.current?.trainerId)
    }

    @Test
    fun `convite recebido tambem e o vinculo da tela`() {
        val state = MyTrainerUiState(loading = false, links = listOf(link(LinkStatus.INVITED)))

        assertEquals(LinkStatus.INVITED, state.current?.status)
    }

    @Test
    fun `pausado vale mais que um pedido a outro treinador`() {
        val state = MyTrainerUiState(
            loading = false,
            links = listOf(
                link(LinkStatus.REQUESTED, trainerId = "treinador-2"),
                link(LinkStatus.PAUSED, trainerId = "treinador-1"),
            ),
        )

        // Pausa é combinada e volta; o pedido a um terceiro ainda não é nada.
        assertEquals("treinador-1", state.current?.trainerId)
    }

    @Test
    fun `procurar so vale com codigo digitado`() {
        assertFalse(MyTrainerUiState(loading = false).canSubmitCode)
        assertTrue(MyTrainerUiState(loading = false, code = "ABC234").canSubmitCode)
        assertFalse(
            "com uma busca em curso não há o que repetir",
            MyTrainerUiState(loading = false, code = "ABC234", checking = true).canSubmitCode,
        )
    }
}
