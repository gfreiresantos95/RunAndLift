package com.gabrielfreire.runandlift.feature.student.navigation

import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * O que o grafo do aluno precisa para montar os seus ViewModels.
 *
 * Existe para o grafo não crescer um parâmetro por repositório a cada tela nova: o que muda passa a
 * ser o conteúdo desta classe, e não a assinatura de [studentGraph] e a de quem a chama.
 *
 * Quem constrói é `:app`, que tem o container de dependências — este módulo apenas recebe.
 */
internal data class StudentDependencies(val authRepository: AuthRepository, val userRepository: UserRepository)
