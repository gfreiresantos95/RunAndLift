package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O id do vínculo e os mapas que vão para `links/{id}`.
 *
 * **É o teste da convenção do ADR-0007**, que é a regra deste módulo com a falha mais silenciosa de
 * todas: quebrada, ela não dá erro nenhum — grava um documento perfeitamente válido que nenhuma
 * Security Rule consegue encontrar depois, e o sintoma aparece longe daqui, como um treinador que
 * não enxerga o aluno que acabou de aceitar.
 *
 * O outro par de olhos sobre o mesmo assunto está em `firestore/rules.test.js`, contra o emulador.
 * Aqui se afirma o que o app **escreve**; lá, o que o servidor **aceita**.
 *
 * O último grupo é o caminho de volta, e ele tem uma regra própria: **documento estranho vira
 * ausência na lista, e não exceção**. Uma linha perdida é menos grave que a carteira inteira
 * falhando — e é o que separa "um vínculo sumiu" de "o app não abre a aba de alunos".
 */
class LinkDocumentTest {

    @Test
    fun `o id e trainerId sublinhado studentId, nessa ordem`() {
        assertEquals("treinador-1_aluno-1", LinkDocument.id(trainerId = "treinador-1", studentId = "aluno-1"))
    }

    @Test
    fun `id invertido nao e o mesmo id`() {
        val certo = LinkDocument.id(trainerId = "t", studentId = "a")
        val invertido = LinkDocument.id(trainerId = "a", studentId = "t")

        assertFalse(
            "a regra monta o caminho com o treinador primeiro; invertido, ela aponta para o vazio",
            certo == invertido,
        )
    }

    @Test
    fun `o vinculo nasce com os dois identificadores tambem como campos`() {
        val fields = LinkDocument.fields(link(LinkStatus.REQUESTED))

        // A regra lê os dois para responder quem confirma, e `resource.id` não se parte em rule.
        assertEquals("treinador-1", fields[LinkDocument.FIELD_TRAINER_ID])
        assertEquals("aluno-1", fields[LinkDocument.FIELD_STUDENT_ID])
    }

    @Test
    fun `estado e origem vao em minusculo, que e o literal que a regra compara`() {
        val fields = LinkDocument.fields(link(LinkStatus.REQUESTED))

        assertEquals("requested", fields[LinkDocument.FIELD_STATUS])
        assertEquals("invite_code", fields[LinkDocument.FIELD_ORIGIN])
    }

    @Test
    fun `os nomes viajam dentro do vinculo, porque users e ilegivel para a contraparte`() {
        val fields = LinkDocument.fields(link(LinkStatus.REQUESTED))

        assertEquals("Carlos Pereira", fields[LinkDocument.FIELD_TRAINER_NAME])
        assertEquals("Ana Souza", fields[LinkDocument.FIELD_STUDENT_NAME])
    }

    @Test
    fun `a mudanca de estado nao reenvia identificador nenhum`() {
        val fields = LinkDocument.statusFields(LinkStatus.ACTIVE)

        assertEquals("active", fields[LinkDocument.FIELD_STATUS])
        assertNull(
            "a regra exige que os identificadores cheguem iguais; reenviá-los é reenviar a chance de errar",
            fields[LinkDocument.FIELD_TRAINER_ID],
        )
        assertNull(fields[LinkDocument.FIELD_STUDENT_ID])
    }

    @Test
    fun `reabrir preserva a data em que essas duas pessoas se encontraram`() {
        val fields = LinkDocument.renewFields(link(LinkStatus.REQUESTED))

        assertEquals("requested", fields[LinkDocument.FIELD_STATUS])
        assertNull("retomar não recomeça o histórico", fields[LinkDocument.FIELD_CREATED_AT])
        assertTrue("mas registra quando foi retomado", fields.containsKey(LinkDocument.FIELD_UPDATED_AT))
    }

    @Test
    fun `reabrir atualiza os nomes, que e a chance de corrigir quem se chamava de outro jeito`() {
        val fields = LinkDocument.renewFields(link(LinkStatus.REQUESTED).copy(studentName = "Ana Souza Lima"))

        assertEquals("Ana Souza Lima", fields[LinkDocument.FIELD_STUDENT_NAME])
    }

    @Test
    fun `o documento gravado volta a ser vinculo`() {
        val link = LinkDocument.link(
            trainerId = "treinador-1",
            studentId = "aluno-1",
            status = "active",
            origin = "showcase",
            trainerName = "Carlos Pereira",
            studentName = "Ana Souza",
        )

        assertEquals(LinkStatus.ACTIVE, link?.status)
        assertEquals(LinkOrigin.SHOWCASE, link?.origin)
        assertEquals("Ana Souza", link?.studentName)
    }

    @Test
    fun `documento sem identificador some da lista, em vez de derruba-la`() {
        // Escrito por uma versão futura ou por engano: uma linha perdida é menos grave que a
        // carteira inteira falhando para quem está tentando trabalhar agora.
        assertNull(LinkDocument.link(trainerId = null, studentId = "aluno-1", status = "active"))
        assertNull(LinkDocument.link(trainerId = "treinador-1", studentId = null, status = "active"))
    }

    @Test
    fun `estado que nao se reconhece some, porque sem ele nao ha o que mostrar`() {
        assertNull(LinkDocument.link("treinador-1", "aluno-1", status = "arquivado"))
        assertNull(LinkDocument.link("treinador-1", "aluno-1", status = null))
    }

    @Test
    fun `origem ausente ou estranha vira convite, e o vinculo entra assim mesmo`() {
        // Descartar um aluno por causa do campo que diz de onde ele veio seria perder um dado
        // central por causa de um estatístico.
        assertEquals(LinkOrigin.INVITE_CODE, LinkDocument.link("t", "a", "active", origin = null)?.origin)
        assertEquals(LinkOrigin.INVITE_CODE, LinkDocument.link("t", "a", "active", origin = "SHOWCASE")?.origin)
    }

    @Test
    fun `nome ausente vira vazio, e nao impede o vinculo de existir`() {
        val link = LinkDocument.link("treinador-1", "aluno-1", "requested")

        // A tela mostra o identificador ou um espaço; ela não deixa de mostrar a linha.
        assertEquals("", link?.trainerName)
        assertEquals("", link?.studentName)
    }

    private fun link(status: LinkStatus) = Link(
        trainerId = "treinador-1",
        studentId = "aluno-1",
        status = status,
        origin = LinkOrigin.INVITE_CODE,
        trainerName = "Carlos Pereira",
        studentName = "Ana Souza",
    )
}
