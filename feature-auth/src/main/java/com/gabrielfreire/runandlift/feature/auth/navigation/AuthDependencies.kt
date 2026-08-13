package com.gabrielfreire.runandlift.feature.auth.navigation

import androidx.compose.runtime.Immutable
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.feature.auth.google.GoogleSignInRequester

/**
 * O que os destinos do grafo precisam e não podem buscar sozinhos.
 *
 * Agrupado porque os três atravessam o grafo inteiro juntos: soltos, cada assinatura repetiria a
 * mesma lista, e nenhum deles significa alguma coisa isolado do fluxo de entrada.
 */
@Immutable
internal data class AuthDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val googleSignIn: GoogleSignInRequester,
)
