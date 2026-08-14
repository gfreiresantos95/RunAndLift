package com.gabrielfreire.runandlift.feature.student.navigation

/**
 * Rotas do grafo do aluno.
 *
 * Público porque `:app` monta o grafo raiz e precisa nomear o destino inicial; o resto do módulo é
 * `internal`. É a mesma fronteira do `:feature-auth`: o que atravessa é o mapa, não as telas.
 *
 * As três rotas são **irmãs**, e não empilhadas — é o que a barra inferior significa. [HOME] é a
 * âncora da pilha: sair de qualquer aba com o botão voltar leva a ela, e voltar de novo sai do app.
 */
object StudentRoutes {
    const val GRAPH = "student"

    const val HOME = "student/home"
    const val WORKOUTS = "student/workouts"
    const val MENU = "student/menu"
}
