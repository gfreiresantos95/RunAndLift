package com.gabrielfreire.runandlift.data

import android.content.Context
import com.gabrielfreire.runandlift.data.auth.AuthRepository
import com.gabrielfreire.runandlift.data.auth.FirebaseAuthRepository
import com.gabrielfreire.runandlift.data.local.RunAndLiftDatabase
import com.gabrielfreire.runandlift.data.location.CachedLocationRepository
import com.gabrielfreire.runandlift.data.location.LocationRepository
import com.gabrielfreire.runandlift.data.remote.catalog.CatalogVersionSource
import com.gabrielfreire.runandlift.data.remote.exercise.FirestoreExerciseRemoteDataSource
import com.gabrielfreire.runandlift.data.remote.location.IbgeLocationRemoteDataSource
import com.gabrielfreire.runandlift.data.repository.ExerciseRepository
import com.gabrielfreire.runandlift.data.repository.OfflineFirstExerciseRepository
import com.gabrielfreire.runandlift.data.student.FirestoreStudentRepository
import com.gabrielfreire.runandlift.data.student.StudentRepository
import com.gabrielfreire.runandlift.data.user.FirestoreUserRepository
import com.gabrielfreire.runandlift.data.user.UserRepository
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Porta de entrada de `:data`. Constrói o banco, as fontes remotas e os repositórios.
 *
 * Existe porque entidades, DAOs e fontes de dados são `internal`: quem está fora do módulo não
 * consegue — nem deve — montar um repositório à mão. O que sai daqui são interfaces e modelos de
 * domínio.
 *
 * Tudo é `by lazy` porque abrir o banco custa I/O, e a `Application` roda antes da primeira tela —
 * o produto promete abertura em ≤2s (E6-01). O banco só é tocado no primeiro uso de verdade.
 *
 * @param catalogVersionSource implementado em `:app` sobre o Remote Config. Ver [CatalogVersionSource].
 */
class DataContainer(
    context: Context,
    catalogVersionSource: CatalogVersionSource,
    dispatchers: AppDispatchers = AppDispatchers(),
) {
    private val applicationContext = context.applicationContext

    private val database: RunAndLiftDatabase by lazy { RunAndLiftDatabase.create(applicationContext) }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val exerciseRepository: ExerciseRepository by lazy {
        OfflineFirstExerciseRepository(
            exerciseDao = database.exerciseDao(),
            catalogMetadataDao = database.catalogMetadataDao(),
            remoteDataSource = FirestoreExerciseRemoteDataSource(firestore),
            catalogVersionSource = catalogVersionSource,
            dispatchers = dispatchers,
        )
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(firebaseAuth = firebaseAuth, dispatchers = dispatchers)
    }

    val userRepository: UserRepository by lazy {
        FirestoreUserRepository(firestore = firestore, dispatchers = dispatchers)
    }

    val studentRepository: StudentRepository by lazy {
        FirestoreStudentRepository(firestore = firestore, dispatchers = dispatchers)
    }

    /**
     * O único repositório daqui que não fala com o Firebase — quem responde é a API do IBGE.
     *
     * `by lazy` importa mais neste do que nos outros: ele guarda em memória o que já baixou, e o
     * cache só serve para alguma coisa se a instância for a mesma entre a tela de cadastro, a de
     * perfil e as duas de seleção.
     */
    val locationRepository: LocationRepository by lazy {
        CachedLocationRepository(
            remoteDataSource = IbgeLocationRemoteDataSource(),
            dispatchers = dispatchers,
        )
    }
}
