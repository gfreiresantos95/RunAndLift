package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O que um código digitado vira: vínculo novo, vínculo reaberto, ou recusa.
 *
 * É a regra que decide quando alguém passa a poder ler a anamnese de outra pessoa, e por isso ela
 * não pode viver só dentro do adaptador do Firestore — onde nenhum teste de JVM a alcança, e onde a
 * única forma de afirmá-la seria reproduzi-la dentro de um dublê.
 *
 * O terceiro teste guarda a regra que sustenta o resto do fluxo: **o pedido nasce pendente**, mesmo
 * vindo do código do próprio treinador. É a confirmação dele que separa "alguém digitou meu código"
 * de "tenho um aluno novo".
 */
class LinkRequestTest {

    @Test
    fun `sem vinculo anterior, o pedido cria documento`() {
        val request = LinkRequest.of(INVITE, studentId = "aluno-1", studentName = "Ana Souza", existing = null)

        assertTrue(request is LinkRequest.Create)
    }

    @Test
    fun `vinculo encerrado e reaberto, e nao duplicado`() {
        val request = LinkRequest.of(
            INVITE,
            studentId = "aluno-1",
            studentName = "Ana Souza",
            existing = link(LinkStatus.ENDED),
        )

        // O id é `{trainerId}_{studentId}`: não existe segundo documento possível para o mesmo par.
        assertTrue(request is LinkRequest.Renew)
    }

    @Test
    fun `o pedido nasce pendente, e nunca ativo`() {
        val request = LinkRequest.of(INVITE, studentId = "aluno-1", studentName = "Ana Souza", existing = null)

        assertEquals(LinkStatus.REQUESTED, (request as LinkRequest.Create).link.status)
        assertEquals(LinkOrigin.INVITE_CODE, request.link.origin)
    }

    @Test
    fun `os dois nomes viajam para dentro do vinculo`() {
        val request = LinkRequest.of(INVITE, studentId = "aluno-1", studentName = "Ana Souza", existing = null)
        val link = (request as LinkRequest.Create).link

        // `users/{uid}` é legível só pelo titular: sem esta cópia, a carteira do treinador seria
        // uma lista de identificadores.
        assertEquals("Carlos Pereira", link.trainerName)
        assertEquals("Ana Souza", link.studentName)
    }

    @Test
    fun `codigo do proprio usuario e recusado`() {
        val request = LinkRequest.of(
            INVITE,
            studentId = INVITE.trainerId,
            studentName = "Carlos Pereira",
            existing = null,
        )

        assertEquals(LinkRequestFailure.OWN_CODE, (request as LinkRequest.Rejected).reason)
    }

    @Test
    fun `vinculo vigente recusa um segundo pedido, em qualquer estado`() {
        listOf(LinkStatus.ACTIVE, LinkStatus.PAUSED, LinkStatus.REQUESTED, LinkStatus.INVITED).forEach { status ->
            val request = LinkRequest.of(
                INVITE,
                studentId = "aluno-1",
                studentName = "Ana Souza",
                existing = link(status),
            )

            assertEquals(
                "pedir duas vezes não é erro de quem digitou, e não pode virar um segundo vínculo",
                LinkRequestFailure.ALREADY_LINKED,
                (request as LinkRequest.Rejected).reason,
            )
        }
    }

    private fun link(status: LinkStatus) = Link(
        trainerId = INVITE.trainerId,
        studentId = "aluno-1",
        status = status,
        origin = LinkOrigin.INVITE_CODE,
    )

    private companion object {

        val INVITE = InviteCode(code = "ABC234", trainerId = "treinador-1", trainerName = "Carlos Pereira")
    }
}
