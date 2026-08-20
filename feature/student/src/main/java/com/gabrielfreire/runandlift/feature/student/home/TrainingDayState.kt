package com.gabrielfreire.runandlift.feature.student.home

/**
 * O que aconteceu — ou vai acontecer — num dia da semana do aluno.
 *
 * São três e não dois porque **dia de descanso não é dia perdido**. Uma faixa de sete dias que só
 * distingue "treinou" de "não treinou" acusa a pessoa de quatro faltas por semana justamente quando
 * ela está seguindo o programa à risca; o descanso previsto precisa se ler como parte do plano.
 *
 * A ordem é a da vida do dia: previsto ([PLANNED]), cumprido ([DONE]), ou fora do plano ([REST]).
 */
internal enum class TrainingDayState {

    /** Treino previsto que ainda não aconteceu. Hoje mais tarde, ou depois desta semana. */
    PLANNED,

    /** Treino previsto e registrado. É o único que a faixa marca com visto. */
    DONE,

    /** Sem treino previsto. Descanso, e não falta. */
    REST,
}
