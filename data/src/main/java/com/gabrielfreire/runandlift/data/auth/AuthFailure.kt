package com.gabrielfreire.runandlift.data.auth

/**
 * Por que a autenticação falhou.
 *
 * O repositório traduz os códigos do Firebase para este conjunto fechado: assim a UI não precisa
 * conhecer strings de erro do SDK, e trocar de provedor de autenticação não alcança as telas.
 *
 * A frase que cada motivo vira na tela **não** mora aqui, e é a única extension deste enum que fica
 * de fora: `:data` não tem recursos de string, e uma função `@Composable` neste módulo obrigaria o
 * design system e o backend a compartilhar dependência de UI. Ela vive em `AuthFailureMessage` de
 * `:feature-auth`.
 */
enum class AuthFailure {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    NO_NETWORK,
    TOO_MANY_ATTEMPTS,
    NOT_SIGNED_IN,

    /** Nenhuma conta Google utilizável no aparelho. Tem mensagem própria porque tem solução própria. */
    NO_GOOGLE_ACCOUNT,
    UNKNOWN,
}
