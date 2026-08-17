package com.gabrielfreire.runandlift.feature.trainer.navigation

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * O que o grafo do treinador precisa para montar os seus ViewModels.
 *
 * Existe para o grafo não crescer um parâmetro por repositório a cada tela nova: o que muda passa a
 * ser o conteúdo desta classe, e não a assinatura de [trainerGraph] e a de quem a chama — como
 * aconteceu quando o passo a passo trouxe o perfil profissional e a localidade.
 *
 * Quem constrói é `:app`, que tem o container de dependências; este módulo apenas recebe.
 */
data class TrainerDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val trainerRepository: TrainerRepository,
    val locationRepository: LocationRepository,
)
