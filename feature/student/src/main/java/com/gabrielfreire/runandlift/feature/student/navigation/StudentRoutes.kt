package com.gabrielfreire.runandlift.feature.student.navigation

/**
 * Rotas do grafo do aluno.
 *
 * Público porque `:app` monta o grafo raiz e precisa nomear o destino inicial; o resto do módulo é
 * `internal`. É a mesma fronteira do `:feature:auth`: o que atravessa é o mapa, não as telas.
 *
 * [HOME], [WORKOUTS] e [MENU] são **irmãs**, e é o que a barra inferior significa. [HOME] é a
 * âncora da pilha: sair de qualquer aba com o botão voltar leva a ela, e voltar de novo sai do app.
 *
 * [ONBOARDING] e [PROFILE] ficam **fora** das abas de propósito. As duas são fluxos com começo e
 * fim — uma se percorre uma vez, a outra se abre para corrigir algo e se fecha —, e uma barra
 * inferior no rodapé delas ofereceria uma saída lateral no meio de uma tarefa.
 */
object StudentRoutes {
    const val GRAPH = "student"

    const val HOME = "student/home"
    const val WORKOUTS = "student/workouts"
    const val MENU = "student/menu"

    /**
     * Passo a passo do primeiro acesso. É o destino inicial de quem ainda não tem documento em
     * `students/{uid}` — quem decide isso é `:app`, na abertura, antes da primeira composição.
     */
    const val ONBOARDING = "student/onboarding"

    /** Edição do perfil de treino, alcançada pelo aviso da home e pelo menu. */
    const val PROFILE = "student/profile"

    /**
     * Dados cadastrais — nome e contato, em `users/{uid}`.
     *
     * Rota separada de [PROFILE] porque são dois documentos com dois públicos: este só o titular
     * lê, e aquele o treinador vinculado também. Uma tela só esconderia a diferença de quem precisa
     * dela para decidir o que preencher.
     */
    const val ACCOUNT = "student/account"
}
