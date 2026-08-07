package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Documento `users/{uid}` — papéis e papel ativo (backlog E1-02, E1-08, E1-09).
 *
 * Custo declarado: [profile] gasta **0 leitura** quando o documento já está no cache do Firestore,
 * e 1 quando não está. É a regra 3 do orçamento (§2.4) aplicada onde ela cabe: papel do usuário
 * muda raramente, e ler do servidor a cada abertura seria desperdício.
 */
interface UserRepository {

    /** Perfil do usuário, ou `null` se ainda não houver documento (conta recém-criada). */
    suspend fun profile(uid: String): UserProfile?

    /**
     * Grava o papel escolhido no onboarding. Preserva o papel que já existia — é o que permite
     * o mesmo usuário virar treinador e aluno sem segunda conta (§3.2).
     */
    suspend fun addRole(uid: String, role: ActiveRole, displayName: String?): UserProfile

    /** Troca o papel ativo, sem alterar os papéis que a conta possui. */
    suspend fun setActiveRole(uid: String, role: ActiveRole)
}

internal class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : UserRepository {

    override suspend fun profile(uid: String): UserProfile? = withContext(dispatchers.io) {
        val document = runCatching { document(uid).get(Source.CACHE).await() }
            .getOrNull()
            ?.takeIf { it.exists() }
            ?: document(uid).get(Source.SERVER).await()

        if (!document.exists()) return@withContext null

        UserProfile(
            uid = uid,
            displayName = document.getString(FIELD_DISPLAY_NAME),
            roles = UserRoles(
                trainer = document.getBoolean(FIELD_ROLE_TRAINER) ?: false,
                student = document.getBoolean(FIELD_ROLE_STUDENT) ?: false,
            ),
            activeRole = ActiveRole.fromStorage(document.getString(FIELD_ACTIVE_ROLE)),
        )
    }

    override suspend fun addRole(uid: String, role: ActiveRole, displayName: String?): UserProfile =
        withContext(dispatchers.io) {
            val existing = profile(uid)
            val roles = UserRoles(
                trainer = existing?.roles?.trainer == true || role == ActiveRole.TRAINER,
                student = existing?.roles?.student == true || role == ActiveRole.STUDENT,
            )

            // Mapa aninhado, e não chave "roles.trainer": em `set()` o ponto é parte do nome do
            // campo, não caminho — só `update()` o interpreta como caminho. Com a chave achatada
            // o Firestore criaria um campo literalmente chamado "roles.trainer".
            val updates = mutableMapOf<String, Any>(
                FIELD_ROLES to mapOf(
                    FIELD_TRAINER to roles.trainer,
                    FIELD_STUDENT to roles.student,
                ),
                FIELD_ACTIVE_ROLE to role.storageValue,
            )
            displayName?.let { updates[FIELD_DISPLAY_NAME] = it }

            document(uid).set(updates, SetOptions.merge()).await()

            UserProfile(
                uid = uid,
                displayName = displayName ?: existing?.displayName,
                roles = roles,
                activeRole = role,
            )
        }

    override suspend fun setActiveRole(uid: String, role: ActiveRole) = withContext(dispatchers.io) {
        document(uid).update(FIELD_ACTIVE_ROLE, role.storageValue).await()
        Unit
    }

    private fun document(uid: String) = firestore.collection(COLLECTION).document(uid)

    private companion object {
        const val COLLECTION = "users"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_ACTIVE_ROLE = "activeRole"
        const val FIELD_ROLES = "roles"
        const val FIELD_TRAINER = "trainer"
        const val FIELD_STUDENT = "student"

        // Na LEITURA o ponto é caminho, então aqui a forma achatada está correta.
        const val FIELD_ROLE_TRAINER = "$FIELD_ROLES.$FIELD_TRAINER"
        const val FIELD_ROLE_STUDENT = "$FIELD_ROLES.$FIELD_STUDENT"
    }
}
