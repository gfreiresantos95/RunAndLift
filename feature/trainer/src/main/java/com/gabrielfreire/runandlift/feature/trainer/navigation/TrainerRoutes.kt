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
 * [HOME], [STUDENTS], [WORKOUTS] e [MENU] são **irmãs**, e é o que a barra inferior significa.
 * [ONBOARDING], [PROFILE], [ACCOUNT] e [INVITE] ficam **fora** das abas de propósito: são fluxos com
 * começo e fim — um se percorre uma vez, os outros se abrem para resolver algo e se fecham —, e uma
 * barra inferior no rodapé deles ofereceria uma saída lateral no meio de uma tarefa.
 */
object TrainerRoutes {
    const val GRAPH = "trainer"

    const val HOME = "trainer/home"

    /** Carteira de alunos: quem pediu, quem treina e quem saiu. */
    const val STUDENTS = "trainer/students"

    const val WORKOUTS = "trainer/workouts"
    const val MENU = "trainer/menu"

    /**
     * O código de convite, aberto pela carteira.
     *
     * Tela e não caixa de diálogo: o código existe para ser lido em voz alta, copiado e enviado, e
     * uma caixa que some ao toque fora dela é o pior lugar possível para uma coisa que se copia.
     */
    internal const val INVITE = "trainer/invite"

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

    // ---------- montagem de treino ----------

    private const val PROGRAM = "trainer/program"

    /** Argumento do editor: o programa a abrir, ou [NEW_PROGRAM] para começar um do zero. */
    internal const val PROGRAM_ID_ARG = "programId"

    /** Argumento do editor de dia e da prescrição: a posição do dia dentro do programa. */
    internal const val DAY_INDEX_ARG = "dayIndex"

    /** Argumento da prescrição: a posição do exercício dentro do dia. */
    internal const val EXERCISE_INDEX_ARG = "exerciseIndex"

    /**
     * O id que significa "ainda não existe".
     *
     * Uma palavra reservada no lugar de um id, e não uma rota separada para criar: as duas telas
     * seriam a mesma, com o mesmo formulário e a mesma gravação, e a única diferença — ler antes de
     * mostrar — cabe num `if`. É a mesma razão de o cadastro e a conclusão de perfil compartilharem
     * `ProfileFormState`.
     *
     * O valor não colide com id de documento do Firestore porque eles têm 20 caracteres.
     */
    internal const val NEW_PROGRAM = "novo"

    /**
     * Editor do programa. É a **raiz da montagem**, e isso tem consequência técnica.
     *
     * O editor de dia e a prescrição são empilhados por cima desta entrada e compartilham o
     * ViewModel dela — o programa inteiro fica em memória enquanto se monta, e só vai ao Firestore
     * quando alguém salva. Ver `sharedProgramEditorViewModel`.
     */
    internal const val PROGRAM_EDITOR_PATTERN = "$PROGRAM/{$PROGRAM_ID_ARG}"

    internal fun programEditor(programId: String = NEW_PROGRAM): String = "$PROGRAM/$programId"

    internal const val DAY_EDITOR_PATTERN = "$PROGRAM_EDITOR_PATTERN/day/{$DAY_INDEX_ARG}"

    internal fun dayEditor(programId: String, dayIndex: Int): String = "${programEditor(programId)}/day/$dayIndex"

    internal const val PRESCRIPTION_PATTERN =
        "$DAY_EDITOR_PATTERN/exercise/{$EXERCISE_INDEX_ARG}"

    internal fun prescription(programId: String, dayIndex: Int, exerciseIndex: Int): String =
        "${dayEditor(programId, dayIndex)}/exercise/$exerciseIndex"

    /**
     * Atribuir o programa a um aluno, aberta pelo editor.
     *
     * Empilhada sobre o editor como as outras, mas **não compartilha o ViewModel dele**: o que ela
     * precisa do programa é a versão gravada, não o rascunho — atribuir uma edição que ainda não
     * foi salva daria ao aluno um treino que não existe em `programs`.
     */
    internal const val ASSIGN_PATTERN = "$PROGRAM_EDITOR_PATTERN/assign"

    internal fun assign(programId: String): String = "${programEditor(programId)}/assign"

    /**
     * O catálogo de exercícios, aberto pelo editor de dia.
     *
     * Rota sem argumento: o que a tela escolhe volta pela entrada anterior da pilha, como nas listas
     * de estado e cidade. Quem sabe em que dia o exercício entra é quem a abriu.
     */
    internal const val CATALOG = "trainer/catalog"

    internal const val EXERCISE_ID_ARG = "exerciseId"

    private const val EXERCISE = "trainer/exercise"

    /** Detalhe de um exercício do catálogo, aberto pela lista. */
    internal const val EXERCISE_DETAIL_PATTERN = "$EXERCISE/{$EXERCISE_ID_ARG}"

    internal fun exerciseDetail(exerciseId: String): String = "$EXERCISE/$exerciseId"
}
