package com.gabrielfreire.runandlift.feature.trainer.students

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * A carteira de alunos, repartida em três blocos.
 *
 * A divisão é a decisão principal desta tela, e ela não vem do banco: o repositório devolve uma
 * lista só, com tudo. **Quem espera resposta do treinador vem primeiro** porque é a única parte da
 * tela que pede uma ação dele hoje — uma lista única por ordem alfabética esconderia um pedido novo
 * entre trinta nomes conhecidos.
 *
 * Encerrados continuam aparecendo, no fim. Um aluno que some de uma lista sem explicação vira
 * dúvida ("cancelei sem querer?"), e o histórico é justamente o que o vínculo encerrado preserva.
 *
 * A ordenação e o corte moram aqui, e não no repositório, por dois motivos: são decisão de tela, e
 * aqui podem ser afirmados por um teste que não precisa de Firestore nenhum.
 *
 * @param failed leitura que não respondeu. Diferente de [isEmpty] de propósito: "você ainda não tem
 *   alunos" e "não consegui carregar sua carteira" são a mesma tela em branco e duas conversas
 *   completamente diferentes.
 */
internal data class StudentsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val links: List<Link> = emptyList(),
    val updating: String? = null,
) {

    /** Pedidos e convites — o que espera decisão de alguém. Sempre no topo. */
    val pending: List<Link>
        get() = links.filter { it.isPending }.sortedByName()

    /** Quem treina agora, ativos antes de pausados: pausado é exceção, e exceção não abre lista. */
    val current: List<Link>
        get() = links
            .filter { it.status == LinkStatus.ACTIVE || it.status == LinkStatus.PAUSED }
            .sortedWith(compareBy({ it.status != LinkStatus.ACTIVE }, { it.studentName.sortKey() }))

    /** Quem saiu. Fica por último, e fica. */
    val past: List<Link>
        get() = links.filter { it.status == LinkStatus.ENDED }.sortedByName()

    /** Carteira vazia de verdade — nem pedido, nem aluno, nem histórico. */
    val isEmpty: Boolean
        get() = links.isEmpty()

    /** Se este vínculo está esperando a gravação de uma mudança de estado terminar. */
    fun isUpdating(link: Link): Boolean = updating == link.studentId

    /**
     * Nome vazio vai para o fim, e não para o começo.
     *
     * Ordenar por texto vazio poria justamente as linhas sem nome — as que menos ajudam a
     * reconhecer alguém — na primeira posição da lista.
     */
    private fun List<Link>.sortedByName(): List<Link> = sortedBy { it.studentName.sortKey() }

    private fun String.sortKey(): String = ifBlank { LAST }.lowercase()

    private companion object {

        /** O maior caractere que existe: qualquer nome real vem antes dele. */
        const val LAST = "￿"
    }
}
