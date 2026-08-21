package com.gabrielfreire.runandlift.data.model

/**
 * Documento `programs/{programId}` — o molde de treino que o treinador monta.
 *
 * **É um molde, e não a prescrição de alguém.** O treinador monta uma vez e atribui a quantos alunos
 * fizer sentido; o que cada aluno recebe é uma cópia, em `assignments`, que pode ser ajustada
 * individualmente sem tocar aqui. Editar o molde depois **não** muda o treino de quem já está
 * treinando com ele, e isso é intencional: prescrição é ato profissional, e ela mudar sozinha na
 * madrugada é o comportamento errado.
 *
 * **Os dias vêm dentro do documento**, e não numa subcoleção. É a regra 2 do orçamento de leitura
 * (§2.4): um programa inteiro custa **uma leitura**, não uma mais o número de dias mais o número de
 * exercícios. Um programa de seis dias com dez exercícios cada cabe folgado no teto de 1 MB de um
 * documento do Firestore — a conta dá cerca de 20 KB.
 *
 * A regra de acesso é a mais simples de todo o projeto e tem uma consequência grande:
 * `allow read: if isSelf(resource.data.trainerId)` — **o aluno não consegue ler esta coleção**. É
 * por isso que `assignments` carrega uma cópia dos dias em vez de apontar para cá.
 *
 * @param id o identificador do documento. Vazio enquanto o programa nunca foi salvo.
 * @param goal o objetivo declarado, reaproveitando o mesmo enum que o aluno escolhe no perfil — o
 *   que permite, mais adiante, casar um programa com quem o pediu.
 * @param updatedAt milissegundos do relógio do servidor. Serve para ordenar a lista de programas
 *   pelo que se mexeu por último, que é quase sempre o que se quer abrir de novo.
 */
data class Program(
    val id: String,
    val trainerId: String,
    val name: String,
    val goal: TrainingGoal? = null,
    val notes: String? = null,
    val days: List<ProgramDay> = emptyList(),
    val updatedAt: Long = 0L,
) {

    /** Quantos exercícios o programa tem somando todos os dias. */
    val totalExercises: Int get() = days.sumOf { it.exercises.size }

    /**
     * Se o programa está em condição de ser atribuído a alguém.
     *
     * Um programa sem nome não se acha na lista depois, e um dia sem exercício é uma promessa vazia
     * para quem abrir o treino na academia. Ficam os dois como bloqueio de atribuição — **não** de
     * salvamento: montar um programa leva dias, e um app que se recusa a guardar trabalho pela
     * metade ensina a pessoa a não confiar nele.
     */
    val isAssignable: Boolean
        get() = name.isNotBlank() && days.isNotEmpty() && days.none { it.isEmpty }
}
