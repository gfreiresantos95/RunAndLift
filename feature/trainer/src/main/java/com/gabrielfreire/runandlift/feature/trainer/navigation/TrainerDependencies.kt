package com.gabrielfreire.runandlift.feature.trainer.navigation

import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.program.ProgramRepository
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
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
    /** O vínculo com os alunos. É o primeiro repositório que os dois grafos de papel compartilham. */
    val linkRepository: LinkRepository,
    /** O treino que cada aluno recebeu, em `assignments`. É a junção entre o molde e a pessoa. */
    val assignmentRepository: AssignmentRepository,
    /** Os moldes de treino, em `programs`. Só o treinador escreve e só ele lê. */
    val programRepository: ProgramRepository,
    /**
     * O catálogo de exercícios, servido do Room.
     *
     * É a primeira vez que ele sai de `:data` — existe desde o E0-03 e nunca foi consumido. Nada
     * nele precisou mudar para a montagem de treino: ler não toca a rede, a busca roda no SQLite e
     * sincronizar é uma chamada à parte.
     */
    val exerciseRepository: ExerciseRepository,
)
