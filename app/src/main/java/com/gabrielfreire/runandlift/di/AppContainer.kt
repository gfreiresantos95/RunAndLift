package com.gabrielfreire.runandlift.di

import android.content.Context
import com.gabrielfreire.runandlift.data.DataContainer
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.link.LinkRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository
import com.gabrielfreire.runandlift.data.user.UserRepository

/**
 * Grafo de dependências do aplicativo, criado uma vez em
 * [com.gabrielfreire.runandlift.RunAndLiftApplication] e vivo enquanto o processo existir.
 *
 * **Injeção manual, não Hilt nem Koin** (ADR-0003). Enquanto o grafo couber em um arquivo legível,
 * um framework de DI cobraria processamento de anotação sem resolver problema que exista.
 *
 * Como cresce: cada dependência vira uma propriedade `by lazy` aqui, e o ViewModel que precisa dela
 * ganha um parâmetro de construtor mais uma `viewModelFactory` que lê o container via
 * `APPLICATION_KEY`.
 *
 * Quando trocar por Hilt: quando houver escopo por papel (treinador/aluno), `HiltWorker` para a
 * fila de sincronização (E0-04), ou dependência que precise viver menos que o processo.
 */
class AppContainer(context: Context) {

    private val dataContainer = DataContainer(
        context = context,
        catalogVersionSource = RemoteConfigCatalogVersionSource(),
    )

    /** Catálogo de exercícios, servido do banco local (backlog E0-03, E4-03). */
    val exerciseRepository: ExerciseRepository get() = dataContainer.exerciseRepository

    /** Autenticação (E1-01, E1-10). */
    val authRepository: AuthRepository get() = dataContainer.authRepository

    /** Papéis e papel ativo (E1-02, E1-09). */
    val userRepository: UserRepository get() = dataContainer.userRepository

    /** Perfil de treino do aluno, em `students/{uid}` (E2-01). */
    val studentRepository: StudentRepository get() = dataContainer.studentRepository

    /** Perfil profissional do treinador, em `trainerProfiles/{uid}` (E3-02). */
    val trainerRepository: TrainerRepository get() = dataContainer.trainerRepository

    /** Vínculo entre treinador e aluno, em `links` e `inviteCodes` (E3-03). */
    val linkRepository: LinkRepository get() = dataContainer.linkRepository

    /**
     * Estados e municípios, servidos da API do IBGE com cache em memória.
     *
     * O `get()` delega ao container de `:data`, que o guarda em `by lazy`: é a mesma instância para
     * o cadastro, para "Meus dados" e para as telas de seleção, e é isso que faz o cache valer —
     * uma instância por tela baixaria a mesma lista de novo em cada uma.
     */
    val locationRepository: LocationRepository get() = dataContainer.locationRepository
}
