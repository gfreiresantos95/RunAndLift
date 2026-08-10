package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
     * Quando [SignUpDetails.cref] vem preenchido, a mesma chamada abre `trainerProfiles/{uid}`.
     * São **dois documentos porque são dois públicos**: `users/{uid}` só o titular lê, e o registro
     * profissional precisa ser legível pelo aluno vinculado. As duas escritas vão num
     * [com.google.firebase.firestore.WriteBatch] — meia gravação deixaria um treinador com papel e
     * sem registro.
     *
     * Custo declarado: 1 escrita (2 com registro profissional, numa ida só), mais a leitura de
     * [profile] para descobrir os papéis atuais — **0 do orçamento** quando o documento está no
     * cache, que é o caso logo após o cadastro.
     *
     * @param role papel a somar. `null` grava apenas a identidade, para o cadastro que ainda não
     *   sabe o papel — a escolha vem na tela seguinte.
     */
    suspend fun saveProfile(uid: String, role: ActiveRole?, details: SignUpDetails = SignUpDetails()): UserProfile

    /** Troca o papel ativo, sem alterar os papéis que a conta possui. */
    suspend fun setActiveRole(uid: String, role: ActiveRole)

    /**
     * Registro no CREF gravado em `trainerProfiles/{uid}`, ou `null` quando não há nenhum.
     *
     * Existe separado de [profile] porque mora em outro documento, e porque só interessa a quem é
     * treinador — cobrar essa leitura de todo aluno seria pagar por um dado que ele não tem.
     *
     * Custo declarado: **0 leitura** com o documento em cache, 1 quando não está. Chamado no
     * caminho frio de conferir se o cadastro do treinador está completo, nunca em tela de treino.
     */
    suspend fun trainerRegistration(uid: String): String?
}

internal class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : UserRepository {

    override suspend fun profile(uid: String): UserProfile? = withContext(dispatchers.io) {
        val document = readCacheFirst(document(uid))

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
            acceptedTermsVersion = document.getString(FIELD_CONSENT_TERMS_VERSION),
        )
    }

    override suspend fun trainerRegistration(uid: String): String? = withContext(dispatchers.io) {
        readCacheFirst(trainerDocument(uid)).getString(FIELD_CREF)
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

            firestore.batch()
                .apply {
                    set(
                        document(uid),
                        fieldsFor(roles, role, details.copy(displayName = displayName)),
                        SetOptions.merge(),
                    )
                    details.cref?.let { set(trainerDocument(uid), mapOf(FIELD_CREF to it), SetOptions.merge()) }
                }
                .commit()
                .await()

            UserProfile(
                uid = uid,
                displayName = displayName ?: existing?.displayName,
                roles = roles,
                activeRole = role ?: existing?.activeRole,
                birthDate = details.birthDate ?: existing?.birthDate,
                phone = details.phone ?: existing?.phone,
                acceptedTermsVersion = details.consent?.termsVersion ?: existing?.acceptedTermsVersion,
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

    private fun document(uid: String) = firestore.collection(COLLECTION).document(uid)

    /**
     * `trainerProfiles/{uid}` — o que o aluno vinculado pode ler sobre o treinador.
     *
     * O cadastro abre o documento com o registro e mais nada. Vitrine, biografia e especialidades
     * são opt-in (E3-02) e entram por outra tela, com consentimento próprio; escrevê-las aqui
     * colocaria o treinador numa listagem pública que ele não pediu.
     */
    private fun trainerDocument(uid: String) = firestore.collection(TRAINER_COLLECTION).document(uid)

    private companion object {
        const val COLLECTION = "users"
        const val TRAINER_COLLECTION = "trainerProfiles"
        const val FIELD_CREF = "cref"
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
        const val FIELD_CONSENT_TERMS_VERSION = "$FIELD_CONSENTS.$FIELD_TERMS_VERSION"
    }
}
