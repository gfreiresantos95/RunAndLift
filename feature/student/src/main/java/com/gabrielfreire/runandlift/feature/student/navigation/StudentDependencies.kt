package com.gabrielfreire.runandlift.feature.student.navigation

import com.gabrielfreire.runandlift.data.assignment.AssignmentRepository
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * O que o grafo do aluno precisa para montar os seus ViewModels.
 *
 * Existe para o grafo não crescer um parâmetro por repositório a cada tela nova: o que muda passa a
 * ser o conteúdo desta classe, e não a assinatura de [studentGraph] e a de quem a chama — como
 * aconteceu quando o onboarding trouxe o terceiro repositório.
 *
 * Quem constrói é `:app`, que tem o container de dependências; este módulo apenas recebe.
 */
data class StudentDependencies(
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val studentRepository: StudentRepository,
    val locationRepository: LocationRepository,
    /** O vínculo com o treinador. É o primeiro repositório que os dois grafos de papel compartilham. */
    val linkRepository: LinkRepository,
    /**
     * O treino que este aluno recebeu, em `assignments`.
     *
     * O aluno **só lê**: prescrever é ato do treinador, e a regra do Firestore reserva `update` e
     * `delete` a ele. O que chega aqui é a cópia congelada dos dias — não o molde, que o aluno não
     * tem permissão de ler.
     */
    val assignmentRepository: AssignmentRepository,
)
