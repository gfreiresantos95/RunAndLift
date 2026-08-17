package com.gabrielfreire.runandlift.data.link

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * O que um código digitado vira: um vínculo novo, um vínculo reaberto, ou uma recusa.
 *
 * Mora fora de [FirestoreLinkRepository] pela mesma razão de [LinkDocument]: **aqui está a regra, e
 * lá estão as chamadas ao SDK**. Separada, ela é afirmada por um teste comum — e não pelo dublê do
 * repositório, que é onde ela estava começando a existir em segunda cópia. Regra reproduzida dentro
 * de um fake é regra que passa a valer duas vezes e a divergir uma.
 *
 * As três saídas são as três coisas diferentes que o Firestore precisa fazer, e é por isso que são
 * três e não um booleano: criar documento e reabrir documento são escritas distintas — uma manda o
 * mapa inteiro, a outra preserva a data em que essas duas pessoas se encontraram pela primeira vez.
 */
internal sealed interface LinkRequest {

    /** Não havia vínculo nenhum com esse treinador: nasce um documento. */
    data class Create(val link: Link) : LinkRequest

    /** Havia um vínculo encerrado: o **mesmo** documento volta a valer, porque o id é o mesmo. */
    data class Renew(val link: Link) : LinkRequest

    /** O pedido não deve nem ser tentado, e a tela recebe a frase certa por [reason]. */
    data class Rejected(val reason: LinkRequestFailure) : LinkRequest

    companion object {

        /**
         * A decisão, sem tocar em rede.
         *
         * @param existing o vínculo que já existe entre os dois, ou `null`. Vem de fora porque a
         *   tela acabou de listar os próprios vínculos, e porque perguntar isso ao Firestore pelo
         *   caminho direto volta como permissão negada no caso mais comum: a regra de `links`
         *   compara `resource.data`, e documento inexistente faz a regra falhar em vez de responder
         *   "não existe".
         */
        fun of(invite: InviteCode, studentId: String, studentName: String, existing: Link?): LinkRequest {
            val refusal = when {
                // Treinar consigo mesmo é `{uid}_{uid}`: um documento que as regras aceitariam, e
                // o resultado seria a pessoa na própria carteira.
                invite.trainerId == studentId -> LinkRequestFailure.OWN_CODE

                existing != null && existing.status != LinkStatus.ENDED -> LinkRequestFailure.ALREADY_LINKED

                else -> null
            }

            if (refusal != null) return Rejected(refusal)

            // Nasce pendente mesmo vindo do código do próprio treinador: o código pode ter sido
            // repassado, e a confirmação dele é o que separa "alguém digitou meu código" de "tenho
            // um aluno novo".
            val link = Link(
                trainerId = invite.trainerId,
                studentId = studentId,
                status = LinkStatus.REQUESTED,
                origin = LinkOrigin.INVITE_CODE,
                trainerName = invite.trainerName,
                studentName = studentName,
            )

            return if (existing == null) Create(link) else Renew(link)
        }
    }
}
