package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Documento `users/{uid}` — identidade, papéis e consentimento (backlog E1-02, E1-08, E1-09).
 *
 * Custo declarado: [profile] gasta **0 leitura** quando o documento já está no cache do Firestore,
 * e 1 quando não está. É a regra 3 do orçamento (§2.4) aplicada onde ela cabe: papel do usuário
 * muda raramente, e ler do servidor a cada abertura seria desperdício.
 */
interface UserRepository {

    /** Perfil do usuário, ou `null` se ainda não houver documento (conta recém-criada). */
    suspend fun profile(uid: String): UserProfile?

    /**
     * Grava identidade e papel de uma vez — é a única escrita do fluxo de entrada.
     *
     * O papel é **somado** ao que já existir, nunca substituído: é o que permite o mesmo usuário
     * ser treinador e aluno de outra pessoa sem segunda conta (§3.2). Campo de [details] que vier
     * nulo não é escrito, então gravar só o papel não apaga o nome que já estava lá.
     *
     * Custo declarado: 1 escrita, mais a leitura de [profile] para descobrir os papéis atuais —
     * **0 do orçamento** quando o documento está no cache, que é o caso logo após o cadastro.
     *
     * @param role papel a somar. `null` grava apenas a identidade, para o cadastro que ainda não
     *   sabe o papel — a escolha vem na tela seguinte.
     */
    suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails = SignUpDetails()): UserProfile

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
            // Data corrompida vira `null` em vez de exceção: um campo mal formado não pode
            // impedir alguém de abrir o app.
            birthDate = document.getString(FIELD_BIRTH_DATE)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            phone = document.getString(FIELD_PHONE),
        )
    }

    override suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails): UserProfile =
        withContext(dispatchers.io) {
            val existing = profile(uid)
            val roles = UserRoles(
                trainer = existing?.roles?.trainer == true || role == ActiveRole.TRAINER,
                student = existing?.roles?.student == true || role == ActiveRole.STUDENT,
            )
            // Cadastro cria, não edita: nome já existente nunca é sobrescrito daqui. Sem isso, a
            // tela de escolha de papel — que deriva um nome do e-mail — apagaria o nome real de
            // quem passou pelo formulário.
            val displayName = details.displayName?.takeIf { existing?.displayName.isNullOrBlank() }

            document(uid)
                .set(fieldsFor(roles, role, details.copy(displayName = displayName)), SetOptions.merge())
                .await()

            UserProfile(
                uid = uid,
                displayName = displayName ?: existing?.displayName,
                roles = roles,
                activeRole = role ?: existing?.activeRole,
                birthDate = details.birthDate ?: existing?.birthDate,
                phone = details.phone ?: existing?.phone,
            )
        }

    override suspend fun setActiveRole(uid: String, role: ActiveRole) = withContext(dispatchers.io) {
        document(uid).update(FIELD_ACTIVE_ROLE, role.storageValue).await()
        Unit
    }

    /**
     * Só o que veio preenchido entra no mapa.
     *
     * `SetOptions.merge()` sobrescreve campo presente e preserva campo ausente — então mandar um
     * `null` explícito apagaria dado bom. Omitir é o que faz uma gravação parcial ser segura.
     *
     * Mapa aninhado, e não a chave `"roles.trainer"`: em `set()` o ponto é parte do nome do campo,
     * não caminho — só `update()` o interpreta como caminho. Com a chave achatada o Firestore
     * criaria um campo literalmente chamado "roles.trainer".
     */
    private fun fieldsFor(roles: UserRoles, role: ActiveRole?, details: SignUpDetails): Map<String, Any> {
        val fields = mutableMapOf<String, Any>(
            FIELD_ROLES to mapOf(FIELD_TRAINER to roles.trainer, FIELD_STUDENT to roles.student),
        )

        role?.let { fields[FIELD_ACTIVE_ROLE] = it.storageValue }
        details.displayName?.let { fields[FIELD_DISPLAY_NAME] = it }
        // Texto ISO, e não Timestamp: data de nascimento não tem hora, e um Timestamp a deslocaria
        // um dia inteiro conforme o fuso de quem lê.
        details.birthDate?.let { fields[FIELD_BIRTH_DATE] = it.toString() }
        details.phone?.let { fields[FIELD_PHONE] = it }
        details.consent?.let { fields[FIELD_CONSENTS] = consentFields(it) }

        return fields
    }

    /**
     * Momento do aceite vem do **servidor**, não do aparelho: prova de consentimento carimbada por
     * um relógio que o titular pode alterar não prova nada.
     */
    private fun consentFields(consent: PrivacyConsent): Map<String, Any> = mapOf(
        FIELD_TERMS_VERSION to consent.termsVersion,
        FIELD_TERMS_ACCEPTED_AT to FieldValue.serverTimestamp(),
        FIELD_MARKETING_OPT_IN to consent.marketingOptIn,
    )

    private fun document(uid: String) = firestore.collection(COLLECTION).document(uid)

    private companion object {
        const val COLLECTION = "users"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_ACTIVE_ROLE = "activeRole"
        const val FIELD_ROLES = "roles"
        const val FIELD_TRAINER = "trainer"
        const val FIELD_STUDENT = "student"
        const val FIELD_BIRTH_DATE = "birthDate"
        const val FIELD_PHONE = "phone"
        const val FIELD_CONSENTS = "consents"
        const val FIELD_TERMS_VERSION = "termsVersion"
        const val FIELD_TERMS_ACCEPTED_AT = "termsAcceptedAt"
        const val FIELD_MARKETING_OPT_IN = "marketingOptIn"

        // Na LEITURA o ponto é caminho, então aqui a forma achatada está correta.
        const val FIELD_ROLE_TRAINER = "$FIELD_ROLES.$FIELD_TRAINER"
        const val FIELD_ROLE_STUDENT = "$FIELD_ROLES.$FIELD_STUDENT"
    }
}
