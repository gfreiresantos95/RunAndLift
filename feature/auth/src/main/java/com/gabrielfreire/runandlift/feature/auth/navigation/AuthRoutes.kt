package com.gabrielfreire.runandlift.feature.auth.navigation

import com.gabrielfreire.runandlift.data.model.ActiveRole

/** Rotas do fluxo de entrada. Públicas porque `:app` decide o destino inicial do grafo raiz. */
object AuthRoutes {
    const val GRAPH = "auth"

    /**
     * Boas-vindas: a bifurcação "sou aluno" / "sou treinador" **antes** de autenticar. É o início
     * do grafo, e não o login, porque o papel decide o funil inteiro de cadastro (E1-02).
     */
    const val WELCOME = "auth/welcome"
    const val RECOVERY = "auth/recovery"

    /**
     * Escolha de papel depois de autenticado. Continua existindo para o caso de a conta chegar
     * aqui sem papel — sessão antiga, primeiro login com Google, ou gravação que falhou no
     * cadastro.
     */
    const val ROLE_SELECTION = "auth/role-selection"

    /**
     * Argumento que carrega o perfil escolhido nas boas-vindas.
     *
     * Ele acompanha as duas telas, e por motivos diferentes: no cadastro é a **intenção** que vai
     * ser gravada; na entrada é só o **caminho percorrido**, exibido como etiqueta. Entrar nunca
     * grava papel — o papel de quem já tem conta vem do `users/{uid}`.
     */
    internal const val ROLE_ARG = "role"

    /**
     * Escolha do estado, numa tela própria.
     *
     * Tela e não lista suspensa porque a de cidade precisa de campo de busca — 853 municípios em
     * Minas Gerais — e as duas têm de se parecer. Ver
     * [com.gabrielfreire.runandlift.core.designsystem.component.AppSearchablePicker].
     */
    internal const val STATE_PICKER = "auth/picker/state"

    /** Argumento da lista de cidades: a sigla do estado cujos municípios listar. */
    internal const val UF_ARG = "uf"

    private const val SIGN_IN = "auth/sign-in"
    private const val SIGN_UP = "auth/sign-up"
    private const val COMPLETE_PROFILE = "auth/complete-profile"
    private const val CITY_PICKER = "auth/picker/city"

    /**
     * Padrões registrados no grafo. O argumento é opcional (sintaxe de query) porque as três telas
     * também são alcançáveis sem perfil conhecido.
     */
    internal const val SIGN_IN_PATTERN = "$SIGN_IN?$ROLE_ARG={$ROLE_ARG}"
    internal const val SIGN_UP_PATTERN = "$SIGN_UP?$ROLE_ARG={$ROLE_ARG}"
    internal const val COMPLETE_PROFILE_PATTERN = "$COMPLETE_PROFILE?$ROLE_ARG={$ROLE_ARG}"

    /**
     * A UF é obrigatória aqui, e por isso vai no caminho e não na consulta: uma lista de municípios
     * sem estado seriam os 5.571 do país inteiro, que é exatamente o que a tela existe para evitar.
     */
    internal const val CITY_PICKER_PATTERN = "$CITY_PICKER/{$UF_ARG}"

    /** Rota concreta da lista de cidades de um estado. */
    internal fun cityPicker(uf: String): String = "$CITY_PICKER/$uf"

    /** Rota concreta da entrada, com ou sem perfil. */
    internal fun signIn(role: ActiveRole? = null): String = withRole(SIGN_IN, role)

    /** Rota concreta do cadastro, com ou sem perfil. */
    internal fun signUp(role: ActiveRole? = null): String = withRole(SIGN_UP, role)

    /**
     * Conclusão de cadastro: o que o provedor de entrada não tinha para dar.
     *
     * Pública porque `:app` também precisa dela — quem abre o app com uma conta pela metade tem de
     * cair aqui, ou fechar o aplicativo viraria a forma de pular a pergunta.
     *
     * @param role papel com que a conta segue. Vai na rota, e não é lido do perfil, porque a conta
     *   recém-criada pelo Google ainda não tem papel gravado: o que existe é a escolha das
     *   boas-vindas, e é esta tela que a grava.
     */
    fun completeProfile(role: ActiveRole? = null): String = withRole(COMPLETE_PROFILE, role)

    private fun withRole(route: String, role: ActiveRole?): String =
        if (role == null) route else "$route?$ROLE_ARG=${role.storageValue}"
}
