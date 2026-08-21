package com.gabrielfreire.runandlift.data.model

/**
 * Um dia do programa — o "Treino A" da planilha.
 *
 * **O dia é solto, e não preso a um dia da semana.** "Treino A" e não "segunda-feira": é como
 * planilha de academia funciona, e é o que não quebra quando alguém perde a segunda e treina na
 * terça. Prender ao calendário deixaria quem furou sem saber se pula ou atrasa tudo, e impediria o
 * mesmo programa de servir a dois alunos com disponibilidades diferentes — que é justamente o que
 * torna um programa reutilizável.
 *
 * @param label o nome curto que a pessoa lê primeiro: "A", "B", "Push". Curto porque aparece dentro
 *   de um marcador na lista, ao lado do foco.
 * @param focus o que o dia treina, em palavras: "Peito e tríceps". Opcional — quem chama os dias de
 *   A, B e C às vezes não precisa de mais nada.
 * @param exercises a ordem importa e é a ordem de execução. Composto antes de isolado é a conta que
 *   o treinador faz; o app guarda a decisão dele, não a refaz.
 */
data class ProgramDay(
    val label: String,
    val focus: String? = null,
    val exercises: List<PrescribedExercise> = emptyList(),
) {

    /** Um dia sem exercício nenhum. É estado normal enquanto se monta, e é bloqueio ao salvar. */
    val isEmpty: Boolean get() = exercises.isEmpty()

    /** Quantas séries o dia inteiro tem. É a medida de volume que cabe numa linha de lista. */
    val totalSets: Int get() = exercises.sumOf { it.sets }
}
