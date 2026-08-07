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
    const val SIGN_IN = "auth/sign-in"
    const val RECOVERY = "auth/recovery"

    /**
     * Escolha de papel depois de autenticado. Continua existindo para o caso de a conta chegar
     * aqui sem papel — sessão antiga, primeiro login com Google, ou gravação que falhou no
     * cadastro.
     */
    const val ROLE_SELECTION = "auth/role-selection"

    /** Argumento que carrega a intenção de papel das boas-vindas até o cadastro. */
    internal const val ROLE_ARG = "role"

    private const val SIGN_UP = "auth/sign-up"

    /**
     * Padrão registrado no grafo. O argumento é opcional (sintaxe de query) porque o cadastro
     * também é alcançável pelo login, onde nenhuma escolha de papel aconteceu.
     */
    internal const val SIGN_UP_PATTERN = "$SIGN_UP?$ROLE_ARG={$ROLE_ARG}"

    /** Rota concreta do cadastro, com ou sem intenção de papel. */
    internal fun signUp(role: ActiveRole? = null): String =
        if (role == null) SIGN_UP else "$SIGN_UP?$ROLE_ARG=${role.storageValue}"
}
