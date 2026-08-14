package com.gabrielfreire.runandlift.feature.auth.navigation

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * Os repositórios que o fluxo de entrada precisa, reunidos.
 *
 * Existe pela mesma razão de [com.gabrielfreire.runandlift.feature.student.navigation
 * .StudentDependencies] no grafo do aluno: sem ele, [authGraph] ganha um parâmetro a cada tela nova
 * que precise de mais um repositório — e a assinatura de quem o chama muda junto. Foi o que
 * aconteceu quando a seleção de estado e cidade trouxe o terceiro.
 *
 * Separado de `AuthDependencies`, que é `internal`, porque aquele carrega também o requisitante da
 * folha do Google — um tipo que não atravessa a fronteira do módulo. Este é o que `:app` monta;
 * aquele é o que os destinos recebem.
 */
data class AuthRepositories(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val locationRepository: LocationRepository,
)
