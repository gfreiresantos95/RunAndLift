package com.gabrielfreire.runandlift.data.assignment

import com.gabrielfreire.runandlift.data.model.Assignment

/**
 * Coleção `assignments` — o treino que cada aluno recebeu.
 *
 * É a junção entre o molde e a pessoa, e a coleção que a regra do Firestore protege dos dois lados:
 * criar exige **vínculo ativo** (uma leitura a mais, e é ela que impede prescrever para quem não é
 * seu aluno), e ler é permitido ao treinador que prescreveu e ao aluno que recebeu.
 *
 * **Sem Room, como `programs`.** A persistência offline que o SDK do Firestore liga sozinha no
 * Android já responde ao caso de reabrir o treino sem sinal, desde que ele tenha sido lido uma vez.
 * A tabela do Room entra junto do registro de série (E6-02), quando houver escrita do aluno para
 * enfileirar — é lá que perder um dado é inaceitável.
 */
interface AssignmentRepository {

    /**
     * Quem já está com este programa, entre os alunos deste treinador.
     *
     * Os dois filtros são de igualdade, então o Firestore os serve juntando índices de campo único —
     * não é preciso índice composto. O filtro por `trainerId` não é desempenho: sem ele a consulta
     * pediria prescrições de outros treinadores, e a regra recusaria a consulta inteira.
     *
     * Custo declarado: 1 leitura por atribuição do programa.
     */
    suspend fun assignmentsOfProgram(trainerId: String, programId: String): List<Assignment>

    /**
     * O treino ativo deste aluno, ou `null` se ele não tem nenhum.
     *
     * Custo declarado: 1 leitura por atribuição do aluno — na prática uma, porque o id do documento
     * é `{trainerId}_{studentId}` e um aluno tem um treinador de cada vez.
     */
    suspend fun activeAssignment(studentId: String): Assignment?

    /**
     * Atribui o programa ao aluno, congelando a cópia dos dias.
     *
     * **Substitui a prescrição anterior daquele par**, porque o id do documento é
     * `{trainerId}_{studentId}`: um aluno tem um treino por treinador, que é o que a tela dele
     * promete ao chamá-lo de "o treino do dia". Reatribuir o mesmo programa depois de editá-lo é o
     * que atualiza o treino de quem já o tinha — ver [Assignment].
     *
     * Custo declarado: 1 escrita, mais **1 leitura da regra** para conferir o vínculo ativo.
     */
    suspend fun assign(assignment: Assignment): Assignment

    /**
     * Encerra a prescrição, sem apagá-la.
     *
     * Encerrado e ausente são coisas diferentes: o documento que fica é o que permite dizer ao aluno
     * que o treino acabou, em vez de a aba dele simplesmente esvaziar de um dia para o outro.
     *
     * Custo declarado: 1 escrita, 0 leitura.
     */
    suspend fun end(assignment: Assignment)

    companion object {

        /**
         * Teto de itens por consulta.
         *
         * Mesmo raciocínio dos outros repositórios: sem teto, o custo de abrir a tela cresce com o
         * sucesso do treinador. Cem é o mesmo teto de `LinkRepository`, e por construção não há como
         * ter mais atribuições de um programa do que alunos.
         */
        const val LIMIT = 100L
    }
}
