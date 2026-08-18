package com.gabrielfreire.runandlift.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A origem do vínculo, ida e volta do banco.
 *
 * São dois valores porque são os dois caminhos que as Security Rules já preveem: o treinador
 * convida, ou o aluno procura. Gravar em minúsculo com separador não é exigência de regra nenhuma
 * aqui — é para o documento não ter metade dos enums em maiúscula e metade em minúscula, que é o
 * tipo de coisa que cobra atenção toda vez que alguém abre o console.
 */
class LinkOriginTest {

    @Test
    fun `os literais gravados acompanham os do estado do vinculo`() {
        assertEquals(listOf("invite_code", "showcase"), LinkOrigin.entries.map { it.stored })
    }

    @Test
    fun `ida e volta preserva a origem`() {
        LinkOrigin.entries.forEach {
            assertEquals(it, LinkOrigin.fromStored(it.stored))
        }
    }

    @Test
    fun `valor desconhecido vira nulo, e nao excecao`() {
        // Quem lê trata a ausência como convite: perder um aluno da lista por causa do campo que
        // diz de onde ele veio seria descartar um dado central por causa de um estatístico.
        assertNull(LinkOrigin.fromStored("INVITE_CODE"))
        assertNull(LinkOrigin.fromStored(null))
    }
}
