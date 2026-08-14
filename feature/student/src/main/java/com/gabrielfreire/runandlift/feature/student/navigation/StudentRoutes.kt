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

    /**
     * Escolha do estado, numa tela própria, aberta por [ACCOUNT].
     *
     * Tela e não lista suspensa porque a de cidade precisa de campo de busca — 853 municípios em
     * Minas Gerais — e as duas têm de se parecer. Ficam **dentro** deste grafo porque o resultado
     * volta pela entrada anterior da pilha: quem as abre é uma tela daqui.
     */
    internal const val STATE_PICKER = "student/picker/state"

    /** Argumento da lista de cidades: a sigla do estado cujos municípios listar. */
    internal const val UF_ARG = "uf"

    private const val CITY_PICKER = "student/picker/city"

    /**
     * A UF é obrigatória, e por isso vai no caminho e não na consulta: uma lista de municípios sem
     * estado seriam os 5.571 do país inteiro, que é o que a tela existe para evitar.
     */
    internal const val CITY_PICKER_PATTERN = "$CITY_PICKER/{$UF_ARG}"

    /** Rota concreta da lista de cidades de um estado. */
    internal fun cityPicker(uf: String): String = "$CITY_PICKER/$uf"
}
