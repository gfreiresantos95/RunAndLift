package com.gabrielfreire.runandlift.data.model

/**
 * Conta autenticada. Identidade crua, sem papel nem perfil — isso é [UserProfile].
 *
 * @param displayName nome que o **provedor** conhece, quando há um: a folha do Google devolve o
 *   nome da conta Google, e o cadastro por e-mail não devolve nada. Não é o nome do perfil, que
 *   mora em `users/{uid}` e é o que o app mostra; este é a única fonte que existe para preencher
 *   aquele quando ninguém digitou nada. Sem carregá-lo até aqui, o nome vindo do Google morre no
 *   SDK e a conta criada por lá fica sem nome para sempre.
 */
data class UserAccount(
    val uid: String,
    val email: String?,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
)
