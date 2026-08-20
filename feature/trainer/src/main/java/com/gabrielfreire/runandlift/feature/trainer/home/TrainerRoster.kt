package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * A carteira contada: quantos alunos ativos, pausados, esperando resposta e encerrados.
 *
 * **É a única parte do painel que vem do banco.** Os vínculos são a coleção que já existe, e contá-
 * los é a diferença entre uma home que mostra o trabalho de quem a abriu e uma home decorativa. O
 * resto do painel ainda é exemplo, porque treino prescrito e aderência dependem de coleções que o
 * produto não tem — e o painel diz qual bloco é qual.
 *
 * **Não custa leitura nenhuma além da que a carteira já faria.** `trainerLinks` devolve a lista
 * inteira, incluindo encerrados, e as quatro contagens saem dela em memória. Uma consulta agregada
 * por estado seriam quatro idas ao servidor para responder o que uma já respondeu — e quando
 * `trainerDashboards` existir, é ele que substitui a varredura, não uma conta a mais.
 *
 * As contas moram aqui e não nos composables pelo motivo de sempre: um teste de JVM alcança este
 * objeto, e não alcança uma tela.
 *
 * @param links tudo o que `LinkRepository.trainerLinks` devolveu, sem filtro.
 */
internal data class TrainerRoster(val links: List<Link>) {

    /** Quem treina agora. É o número que responde "quantos alunos eu tenho?". */
    val active: Int
        get() = links.count { it.status == LinkStatus.ACTIVE }

    /**
     * Quem está esperando alguém responder — o pedido do aluno e o convite ainda não aceito.
     *
     * É o único número do painel que representa uma **tarefa**, e por isso a tela o pinta e o
     * manda para a aba de alunos. Os outros três descrevem uma situação.
     */
    val pending: Int
        get() = links.count { it.isPending }

    /** Suspensos — férias, lesão, uma pausa de pagamento. Voltam, e por isso não somem da conta. */
    val paused: Int
        get() = links.count { it.status == LinkStatus.PAUSED }

    /** Quem saiu. Continua contado porque é histórico, e é responsabilidade técnica mantê-lo. */
    val ended: Int
        get() = links.count { it.status == LinkStatus.ENDED }

    /**
     * Ativos mais pausados: o tamanho real da carteira hoje.
     *
     * Pausado conta porque a vaga continua ocupada — quem pausou volta, e um treinador que declarou
     * capacidade para vinte não tem vinte livres porque três estão de férias.
     */
    val size: Int
        get() = active + paused

    /**
     * Nenhum vínculo de espécie alguma.
     *
     * Diferente de [size] valer zero: quem já teve alunos e encerrou todos não é quem nunca teve
     * nenhum, e o painel tem frases diferentes para os dois — a primeira delas seria um convite a
     * gerar um código que a pessoa já sabe gerar.
     */
    val isEmpty: Boolean
        get() = links.isEmpty()
}
