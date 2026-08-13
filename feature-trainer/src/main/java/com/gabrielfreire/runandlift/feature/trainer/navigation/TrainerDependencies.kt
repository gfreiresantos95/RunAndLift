package com.gabrielfreire.runandlift.feature.trainer.navigation

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * O que o grafo do treinador precisa para montar os seus ViewModels.
 *
 * Quem constrói é `:app`, dono do container; este módulo apenas recebe. Assim uma tela nova muda o
 * conteúdo desta classe, e não a assinatura de [trainerGraph] e a de quem a chama.
 */
internal data class TrainerDependencies(val authRepository: AuthRepository, val userRepository: UserRepository)
