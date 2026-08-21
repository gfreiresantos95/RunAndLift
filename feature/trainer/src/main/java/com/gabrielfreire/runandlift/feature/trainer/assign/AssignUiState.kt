package com.gabrielfreire.runandlift.feature.trainer.assign

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.Program

/**
 * Estado da tela de atribuir um programa.
 *
 * **Só alunos com vínculo ativo entram na lista.** Pausado e encerrado não recebem prescrição — e
 * não é só decisão de tela: a regra do Firestore exige vínculo ativo para criar a atribuição, então
 * mostrá-los aqui seria oferecer um botão que o servidor recusa.
 *
 * @param assignedIds quem **já está** com este programa. A linha desses alunos diz isso em vez de
 *   sumir da lista: quem some vira dúvida ("cadastrei errado?"), e reatribuir é operação legítima —
 *   é o que atualiza o treino de quem já o tinha depois de o molde mudar.
 * @param failed leitura que não respondeu, separada de lista vazia pela razão de sempre: "você não
 *   tem alunos" dito a quem tem trinta e está sem sinal é a pior frase da tela.
 * @param assigning o aluno cuja escrita está em curso, para dois toques não virarem duas escritas.
 */
internal data class AssignUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val assignFailed: Boolean = false,
    val program: Program? = null,
    val students: List<Link> = emptyList(),
    val assignedIds: Set<String> = emptySet(),
    val assigning: String? = null,
) {

    /** Sem nenhum aluno ativo — e sem falha, que é outra conversa. */
    val isEmpty: Boolean get() = students.isEmpty()

    fun isAssigned(link: Link): Boolean = link.studentId in assignedIds

    fun isAssigning(link: Link): Boolean = assigning == link.studentId
}
