package com.gabrielfreire.runandlift.feature.trainer.navigation

/**
 * Rotas do grafo do treinador.
 *
 * Público porque `:app` monta o grafo raiz; o resto do módulo é `internal`.
 *
 * As rotas do treinador e as do aluno **não se cruzam**, e por serem módulos separados isso deixa
 * de ser disciplina: o código do aluno não enxerga estas constantes nem para escrevê-las por engano.
 */
object TrainerRoutes {
    const val GRAPH = "trainer"

    const val HOME = "trainer/home"
    const val WORKOUTS = "trainer/workouts"
    const val MENU = "trainer/menu"
}
