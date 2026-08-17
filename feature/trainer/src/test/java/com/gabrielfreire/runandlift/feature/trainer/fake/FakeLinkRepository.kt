package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.link.LinkRequestFailure
import com.gabrielfreire.runandlift.data.link.LinkRequestResult
import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * [LinkRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Guarda a lista **em memória e mutável**, e isso não é conveniência: as telas de vínculo releem
 * depois de cada transição, e um fake que sempre devolvesse a mesma lista faria todo teste de
 * "aceitar" passar sem que aceitar mudasse coisa alguma.
 *
 * [failReading] e [failWriting] são `var` porque a rede cai **no meio** de uma sessão: o caso que
 * interessa não é abrir a tela sem sinal, é perder o sinal com a carteira já na tela.
 *
 * @param failReading leitura que não responde — sem rede e sem cache.
 * @param failWriting transição recusada, que é o que uma Security Rule faz com uma mudança de estado
 *   que ela não permite.
 */
internal class FakeLinkRepository(
    links: List<Link> = emptyList(),
    private val storedCode: String? = null,
    var failReading: Boolean = false,
    var failWriting: Boolean = false,
) : LinkRepository {

    private val links = links.toMutableList()

    var createdCode: String? = null
        private set

    var lastName: String? = null
        private set

    var createCount: Int = 0
        private set

    override suspend fun trainerLinks(trainerId: String): List<Link> {
        if (failReading) error("sem rede e sem cache")

        return links.filter { it.trainerId == trainerId }
    }

    override suspend fun studentLinks(studentId: String): List<Link> {
        if (failReading) error("sem rede e sem cache")

        return links.filter { it.studentId == studentId }
    }

    override suspend fun inviteCode(trainerId: String): String? {
        if (failReading) error("sem rede e sem cache")

        return storedCode
    }

    override suspend fun createInviteCode(trainerId: String, trainerName: String): String {
        if (failWriting) error("gravação não completou")

        lastName = trainerName
        createCount++

        return "NEW234".also { createdCode = it }
    }

    override suspend fun findInvite(code: String): InviteCode? = null

    override suspend fun requestLink(
        invite: InviteCode,
        studentId: String,
        studentName: String,
        existing: Link?,
    ): LinkRequestResult = LinkRequestResult.Failure(LinkRequestFailure.UNKNOWN)

    override suspend fun updateStatus(link: Link, status: LinkStatus): Link {
        if (failWriting) error("transição recusada")

        val updated = link.copy(status = status)
        val index = links.indexOfFirst { it.trainerId == link.trainerId && it.studentId == link.studentId }

        if (index >= 0) links[index] = updated

        return updated
    }

    companion object {

        /** Um vínculo com nome dos dois lados, que é como ele chega de verdade. */
        fun link(status: LinkStatus, name: String = "Ana Souza", trainerId: String = "u1") = Link(
            trainerId = trainerId,
            studentId = "aluno-$name",
            status = status,
            origin = LinkOrigin.INVITE_CODE,
            trainerName = "Carlos Pereira",
            studentName = name,
        )
    }
}
