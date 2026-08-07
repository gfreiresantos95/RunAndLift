package com.gabrielfreire.runandlift.feature.auth

/** Rotas do fluxo de entrada. Públicas porque `:app` decide o destino inicial do grafo raiz. */
object AuthRoutes {
    const val GRAPH = "auth"
    const val SIGN_IN = "auth/sign-in"
    const val SIGN_UP = "auth/sign-up"
    const val RECOVERY = "auth/recovery"
    const val ROLE_SELECTION = "auth/role-selection"
}
