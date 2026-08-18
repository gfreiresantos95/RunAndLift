package com.gabrielfreire.runandlift.feature.student.fake

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
 * É o gêmeo do fake do módulo do treinador, e a duplicação é a mesma dos outros: conjuntos de teste
 * não são compartilhados entre módulos, e trinta linhas repetidas custam menos que um módulo de
 * fixtures. O gatilho para extrair continua sendo o terceiro módulo que precisar deles.
 *
 * **Não reproduz as regras do pedido**, e isso é decisão: quem decide entre criar, reabrir e recusar
 * é `LinkRequest`, no `:data`, com teste próprio. Um dublê que refizesse aquele `when` faria a regra
 * existir em duas versões, e a que os testes de tela afirmariam seria a cópia. Aqui a recusa é
 * **dita** por [requestFailure], porque o que estas telas têm de fazer com ela é escolher uma frase.
 *
 * @param invite o convite que [findInvite] encontra. `null` é o código que não existe.
 * @param failReading leitura que não responde. É `var` porque a rede cai no meio da sessão.
 * @param requestFailure a recusa que o repositório real devolveria para este cenário.
 */
internal class FakeLinkRepository(
    links: List<Link> = emptyList(),
    private val invite: InviteCode? = null,
    var failReading: Boolean = false,
    var failWriting: Boolean = false,
    var requestFailure: LinkRequestFailure? = null,
) : LinkRepository {

    private val links = links.toMutableList()

    var lastName: String? = null
        private set

    var requestCount: Int = 0
        private set

    /** O vínculo que a tela disse já conhecer. É o que ela evita ir perguntar ao Firestore. */
    var lastExisting: Link? = null
        private set

    override suspend fun trainerLinks(trainerId: String): List<Link> = links

    override suspend fun studentLinks(studentId: String): List<Link> {
        if (failReading) error("sem rede e sem cache")

        return links.filter { it.studentId == studentId }
    }

    override suspend fun inviteCode(trainerId: String): String? = null

    override suspend fun createInviteCode(trainerId: String, trainerName: String): String = "NEW234"

    override suspend fun findInvite(code: String): InviteCode? {
        if (failReading) error("sem rede e sem cache")

        return invite?.takeIf { it.code == code }
    }

    override suspend fun requestLink(
        invite: InviteCode,
        studentId: String,
        studentName: String,
        existing: Link?,
    ): LinkRequestResult {
        requestCount++
        lastName = studentName
        lastExisting = existing

        val failure = requestFailure ?: LinkRequestFailure.UNKNOWN.takeIf { failWriting }

        if (failure != null) return LinkRequestResult.Failure(failure)

        val link = Link(
            trainerId = invite.trainerId,
            studentId = studentId,
            status = LinkStatus.REQUESTED,
            origin = LinkOrigin.INVITE_CODE,
            trainerName = invite.trainerName,
            studentName = studentName,
        )

        // Reabre em vez de duplicar: o id é `{trainerId}_{studentId}` e não há segundo documento
        // possível para o mesmo par.
        links.removeAll { it.trainerId == invite.trainerId && it.studentId == studentId }
        links += link

        return LinkRequestResult.Success(link)
    }

    override suspend fun updateStatus(link: Link, status: LinkStatus): Link {
        if (failWriting) error("transição recusada")

        val updated = link.copy(status = status)
        val index = links.indexOfFirst { it.trainerId == link.trainerId && it.studentId == link.studentId }

        if (index >= 0) links[index] = updated

        return updated
    }

    companion object {

        const val CODE = "ABC234"

        val INVITE = InviteCode(code = CODE, trainerId = "treinador-1", trainerName = "Carlos Pereira")

        /** Um vínculo com nome dos dois lados, que é como ele chega de verdade. */
        fun link(status: LinkStatus, trainerId: String = "treinador-1") = Link(
            trainerId = trainerId,
            studentId = "u1",
            status = status,
            origin = LinkOrigin.INVITE_CODE,
            trainerName = "Carlos Pereira",
            studentName = "Ana Souza",
        )
    }
}
