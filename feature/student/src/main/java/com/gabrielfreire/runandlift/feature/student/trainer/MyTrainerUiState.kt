package com.gabrielfreire.runandlift.feature.student.trainer

import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * O treinador do aluno — o que existe hoje, e o que ele está tentando criar.
 *
 * A tela tem dois assuntos e um estado só porque eles se alternam: **ou** existe um vínculo que
 * vale, **ou** existe um campo para digitar um código. [current] é o que decide qual dos dois
 * aparece, e por isso ele é uma propriedade derivada e não um campo — dois campos que precisam
 * concordar acabam discordando.
 *
 * @param invite o convite encontrado, esperando confirmação. É o passo entre digitar o código e
 *   pedir o vínculo, e ele existe por um motivo sério: quem pede vínculo está autorizando outra
 *   pessoa a ler a própria anamnese, e conferir um nome antes disso custa uma tela e nenhuma leitura
 *   a mais.
 * @param failed a leitura da lista não respondeu — diferente de não ter treinador nenhum. A primeira
 *   oferece tentar de novo; a segunda oferece digitar um código.
 */
internal data class MyTrainerUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val links: List<Link> = emptyList(),
    val code: String = "",
    val checking: Boolean = false,
    val invite: InviteCode? = null,
    val submitting: Boolean = false,
    val error: TrainerCodeError? = null,
) {

    /**
     * O vínculo que vale agora: ativo, pausado ou esperando alguém confirmar.
     *
     * Um aluno tem um treinador, mas a lista pode trazer mais de um documento — um encerrado do ano
     * passado, um pedido feito ontem. A ordem de [LinkStatus] resolve o empate improvável de dois
     * vigentes: ativo ganha de pausado, que ganha de pendente.
     */
    val current: Link?
        get() = links.filter { it.status != LinkStatus.ENDED }.minByOrNull { it.status.priority }

    /** Vínculos encerrados, do mais recente que se sabe para trás. Histórico, e não lista de opções. */
    val past: List<Link>
        get() = links.filter { it.status == LinkStatus.ENDED }

    /** O campo de código só aparece quando não há vínculo vigente. */
    val canEnterCode: Boolean
        get() = current == null

    /** Com o campo vazio não há o que procurar, e com uma busca em curso não há o que repetir. */
    val canSubmitCode: Boolean
        get() = code.isNotBlank() && !checking && !submitting

    private val LinkStatus.priority: Int
        get() = when (this) {
            LinkStatus.ACTIVE -> 0
            LinkStatus.PAUSED -> 1
            LinkStatus.INVITED -> 2
            LinkStatus.REQUESTED -> 3
            LinkStatus.ENDED -> 4
        }
}
