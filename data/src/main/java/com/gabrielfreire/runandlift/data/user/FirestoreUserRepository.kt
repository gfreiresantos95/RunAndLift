package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * [UserRepository] sobre o Firestore.
 *
 * Fica em arquivo próprio, e não junto da interface, pela mesma razão que separa `AuthRepository`
 * de `FirebaseAuthRepository`: a interface é o contrato que atravessa a fronteira de `:data`, e a
 * implementação é a única parte que conhece coleção, nome de campo e SDK.
 *
 * O que **decide** — o acúmulo de papéis, a trava do nome, os três mapas — mora em [UserDocument],
 * onde um teste comum o alcança.
 */
internal class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : UserRepository {

    override suspend fun profile(uid: String): UserProfile? = withContext(dispatchers.io) {
        val document = readCacheFirst(document(uid))

        if (!document.exists()) return@withContext null

        UserProfile(
            uid = uid,
            displayName = document.getString(UserDocument.FIELD_DISPLAY_NAME),
            roles = UserRoles(
                trainer = document.getBoolean(UserDocument.FIELD_ROLE_TRAINER) ?: false,
                student = document.getBoolean(UserDocument.FIELD_ROLE_STUDENT) ?: false,
            ),
            activeRole = ActiveRole.fromStorage(document.getString(UserDocument.FIELD_ACTIVE_ROLE)),
            // Data corrompida vira `null` em vez de exceção: um campo mal formado não pode
            // impedir alguém de abrir o app.
            birthDate = document.getString(UserDocument.FIELD_BIRTH_DATE)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            phone = document.getString(UserDocument.FIELD_PHONE),
            acceptedTermsVersion = document.getString(UserDocument.FIELD_CONSENT_TERMS_VERSION),
            state = document.getString(UserDocument.FIELD_STATE),
            city = document.getString(UserDocument.FIELD_CITY),
        )
    }

    override suspend fun trainerRegistration(uid: String): String? = withContext(dispatchers.io) {
        readCacheFirst(trainerDocument(uid)).getString(UserDocument.FIELD_CREF)
    }

    override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile =
        withContext(dispatchers.io) {
            val existing = profile(uid)
            val roles = UserDocument.roles(existing?.roles, role)
            val displayName = UserDocument.nameToWrite(existing?.displayName, details.displayName)

            firestore.batch()
                .apply {
                    set(
                        document(uid),
                        UserDocument.fields(roles, role, details.copy(displayName = displayName)),
                        SetOptions.merge(),
                    )
                    details.cref?.let {
                        set(trainerDocument(uid), mapOf(UserDocument.FIELD_CREF to it), SetOptions.merge())
                    }
                }
                .commit()
                .await()

            UserDocument.merged(uid, existing, role, details)
        }

    override suspend fun setActiveRole(uid: String, role: ActiveRole) = withContext(dispatchers.io) {
        document(uid).update(UserDocument.FIELD_ACTIVE_ROLE, role.storageValue).await()
        Unit
    }

    override suspend fun updateIdentity(
        uid: String,
        displayName: String,
        phone: String?,
        state: String?,
        city: String?,
    ) = withContext(dispatchers.io) {
        document(uid).set(UserDocument.identityFields(displayName, phone, state, city), SetOptions.merge()).await()
        Unit
    }

    /**
     * Cache primeiro, servidor só quando não há nada em disco — regra 3 do orçamento (§2.4).
     *
     * A falha do cache é engolida porque "não tem em disco" não é erro; a do servidor **não é**, e
     * chega a quem chamou: sem rede e sem cache, a resposta honesta é "não sei", e quem decide o
     * que fazer com isso é a tela.
     */
    private suspend fun readCacheFirst(reference: DocumentReference): DocumentSnapshot =
        runCatching { reference.get(Source.CACHE).await() }
            .getOrNull()
            ?.takeIf { it.exists() }
            ?: reference.get(Source.SERVER).await()

    private fun document(uid: String) = firestore.collection(UserDocument.COLLECTION).document(uid)

    /**
     * `trainerProfiles/{uid}` — o que o aluno vinculado pode ler sobre o treinador.
     *
     * O cadastro abre o documento com o registro e mais nada. Vitrine, biografia e especialidades
     * são opt-in (E3-02) e entram por outra tela, com consentimento próprio; escrevê-las aqui
     * colocaria o treinador numa listagem pública que ele não pediu.
     */
    private fun trainerDocument(uid: String) = firestore.collection(UserDocument.TRAINER_COLLECTION).document(uid)
}
