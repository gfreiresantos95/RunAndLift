package com.gabrielfreire.runandlift.feature.auth

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

    private const val SIGN_IN = "auth/sign-in"
    private const val SIGN_UP = "auth/sign-up"

    /**
     * Padrões registrados no grafo. O argumento é opcional (sintaxe de query) porque as duas telas
     * também são alcançáveis sem perfil conhecido.
     */
    internal const val SIGN_IN_PATTERN = "$SIGN_IN?$ROLE_ARG={$ROLE_ARG}"
    internal const val SIGN_UP_PATTERN = "$SIGN_UP?$ROLE_ARG={$ROLE_ARG}"

    /** Rota concreta da entrada, com ou sem perfil. */
    internal fun signIn(role: ActiveRole? = null): String = withRole(SIGN_IN, role)

    /** Rota concreta do cadastro, com ou sem perfil. */
    internal fun signUp(role: ActiveRole? = null): String = withRole(SIGN_UP, role)

    private fun withRole(route: String, role: ActiveRole?): String =
        if (role == null) route else "$route?$ROLE_ARG=${role.storageValue}"
}
