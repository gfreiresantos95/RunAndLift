package com.gabrielfreire.runandlift.feature.trainer.navigation

/**
 * Rotas do grafo do treinador.
 *
 * Público porque `:app` monta o grafo raiz e precisa nomear o destino inicial; o resto do módulo é
 * `internal`.
 *
 * As rotas do treinador e as do aluno **não se cruzam**, e por serem módulos separados isso deixa
 * de ser disciplina: o código do aluno não enxerga estas constantes nem para escrevê-las por engano.
 *
 * [HOME], [WORKOUTS] e [MENU] são **irmãs**, e é o que a barra inferior significa. [ONBOARDING],
 * [PROFILE] e [ACCOUNT] ficam **fora** das abas de propósito: são fluxos com começo e fim — um se
 * percorre uma vez, os outros se abrem para corrigir algo e se fecham —, e uma barra inferior no
 * rodapé deles ofereceria uma saída lateral no meio de uma tarefa.
 */
object TrainerRoutes {
    const val GRAPH = "trainer"

    const val HOME = "trainer/home"
    const val WORKOUTS = "trainer/workouts"
    const val MENU = "trainer/menu"

    /**
     * Passo a passo do primeiro acesso como treinador.
     *
     * É o destino inicial de quem ainda não tem o carimbo de conclusão em `trainerProfiles/{uid}` —
     * quem decide isso é `:app`, na abertura, antes da primeira composição. **Não** é a existência
     * do documento que decide, ao contrário do aluno: este já nasce no cadastro, com o registro no
     * CREF dentro.
     */
    const val ONBOARDING = "trainer/onboarding"

    /** Edição do perfil profissional, alcançada pelo aviso da home e pelo menu. */
    const val PROFILE = "trainer/profile"

    /**
     * Dados cadastrais — nome, contato e localidade, em `users/{uid}`.
     *
     * Rota separada de [PROFILE] porque são dois documentos com dois públicos: este só o titular
     * lê, e aquele o aluno vinculado — e, com a vitrine aceita, qualquer pessoa procurando
     * treinador. Uma tela só esconderia a diferença de quem precisa dela para decidir o que
     * preencher.
     */
    const val ACCOUNT = "trainer/account"

    /**
     * Escolha do estado, numa tela própria, aberta por [ACCOUNT].
     *
     * Tela e não lista suspensa porque a de cidade precisa de campo de busca — 853 municípios em
     * Minas Gerais — e as duas têm de se parecer. Ficam **dentro** deste grafo porque o resultado
     * volta pela entrada anterior da pilha: quem as abre é uma tela daqui.
     */
    internal const val STATE_PICKER = "trainer/picker/state"

    /** Argumento da lista de cidades: a sigla do estado cujos municípios listar. */
    internal const val UF_ARG = "uf"

    private const val CITY_PICKER = "trainer/picker/city"

    /**
     * A UF é obrigatória, e por isso vai no caminho e não na consulta: uma lista de municípios sem
     * estado seriam os 5.571 do país inteiro, que é o que a tela existe para evitar.
     */
    internal const val CITY_PICKER_PATTERN = "$CITY_PICKER/{$UF_ARG}"

    /** Rota concreta da lista de cidades de um estado. */
    internal fun cityPicker(uf: String): String = "$CITY_PICKER/$uf"
}
