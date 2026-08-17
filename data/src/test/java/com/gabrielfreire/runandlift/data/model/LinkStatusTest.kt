package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os estados do vínculo, ida e volta do banco.
 *
 * O primeiro teste é o que mais importa e o que menos parece: **os literais gravados são os mesmos
 * que as Security Rules comparam**. `status == 'active'` está escrito em `firestore.rules`, num
 * arquivo que nenhum compilador liga a este; trocar `active` por `ACTIVE` aqui não quebraria a
 * compilação de nada e trancaria todo treinador do lado de fora de todo aluno.
 */
class LinkStatusTest {

    @Test
    fun `os literais gravados sao os que a regra do Firestore compara`() {
        assertEquals(
            listOf("invited", "requested", "active", "paused", "ended"),
            LinkStatus.entries.map { it.stored },
        )
    }

    @Test
    fun `ida e volta preserva o estado`() {
        LinkStatus.entries.forEach {
            assertEquals(it, LinkStatus.fromStored(it.stored))
        }
    }

    @Test
    fun `valor desconhecido vira nulo, e nao excecao`() {
        // Documento escrito por uma versão futura não pode derrubar a carteira de quem está
        // tentando trabalhar agora.
        assertNull(LinkStatus.fromStored("REQUESTED"))
        assertNull(LinkStatus.fromStored("declined"))
        assertNull(LinkStatus.fromStored(null))
    }

    @Test
    fun `pendente e so quem espera alguem confirmar`() {
        assertEquals(
            listOf(LinkStatus.INVITED, LinkStatus.REQUESTED),
            LinkStatus.entries.filter { it.isPending },
        )
    }

    @Test
    fun `nao existe estado recusado, porque recusar e encerrar antes de comecar`() {
        assertTrue(LinkStatus.entries.none { it.stored.contains("declin") || it.stored.contains("reject") })
    }
}
