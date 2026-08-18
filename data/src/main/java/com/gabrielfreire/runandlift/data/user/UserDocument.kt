package com.gabrielfreire.runandlift.data.user

import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.data.model.PrivacyConsent
import com.gabrielfreire.runandlift.data.model.SignUpDetails
import com.gabrielfreire.runandlift.data.model.UserProfile
import com.gabrielfreire.runandlift.data.model.UserRoles
import com.google.firebase.firestore.FieldValue

/**
 * Como a conta vira documento — os nomes dos campos e os três mapas de gravação.
 *
 * Mora fora de [FirestoreUserRepository] pela razão de sempre: **aqui está a regra, e lá estão as
 * chamadas ao SDK**. Duas delas não têm como ser conferidas lendo uma tela e nunca deram erro ao
 * quebrar — o papel que se **acumula** em vez de substituir o anterior, e o nome que só é gravado
 * quando não existe nenhum. A segunda é a que já apagou nome de gente: a tela de escolha de papel
 * deriva um nome do e-mail, e sem a trava ela sobrescreveria o nome real de quem preencheu o
 * formulário.
 */
internal object UserDocument {

    const val COLLECTION = "users"

    /** `trainerProfiles/{uid}` — o cadastro abre o documento com o registro no CREF e mais nada. */
    const val TRAINER_COLLECTION = "trainerProfiles"

    const val FIELD_CREF = "cref"
    const val FIELD_DISPLAY_NAME = "displayName"
    const val FIELD_ACTIVE_ROLE = "activeRole"
    const val FIELD_ROLES = "roles"
    const val FIELD_TRAINER = "trainer"
    const val FIELD_STUDENT = "student"
    const val FIELD_BIRTH_DATE = "birthDate"
    const val FIELD_PHONE = "phone"
    const val FIELD_STATE = "state"
    const val FIELD_CITY = "city"
    const val FIELD_CONSENTS = "consents"
    const val FIELD_TERMS_VERSION = "termsVersion"
    const val FIELD_TERMS_ACCEPTED_AT = "termsAcceptedAt"
    const val FIELD_MARKETING_OPT_IN = "marketingOptIn"

    // Na LEITURA o ponto é caminho, então aqui a forma achatada está correta.
    const val FIELD_ROLE_TRAINER = "$FIELD_ROLES.$FIELD_TRAINER"
    const val FIELD_ROLE_STUDENT = "$FIELD_ROLES.$FIELD_STUDENT"
    const val FIELD_CONSENT_TERMS_VERSION = "$FIELD_CONSENTS.$FIELD_TERMS_VERSION"

    /**
     * Os papéis depois deste cadastro: o que já havia **mais** o que está entrando.
     *
     * Acumula em vez de substituir porque um treinador que também é aluno de outro treinador é caso
     * real e resolvido sem segunda conta. Substituindo, cadastrar-se como aluno tiraria de alguém a
     * carteira de treinador que ele já tinha.
     */
    fun roles(existing: UserRoles?, role: ActiveRole?) = UserRoles(
        trainer = existing?.trainer == true || role == ActiveRole.TRAINER,
        student = existing?.student == true || role == ActiveRole.STUDENT,
    )

    /**
     * O nome a gravar, ou `null` para não tocar no campo.
     *
     * **Cadastro cria, não edita**: nome já existente nunca é sobrescrito daqui. Quem edita o
     * próprio nome passa por `updateIdentity`, que é outra função justamente porque tem outra
     * regra.
     */
    fun nameToWrite(existing: String?, provided: String?): String? = provided?.takeIf { existing.isNullOrBlank() }

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
    fun fields(roles: UserRoles, role: ActiveRole?, details: SignUpDetails): Map<String, Any> {
        val fields = mutableMapOf<String, Any>(
            FIELD_ROLES to mapOf(FIELD_TRAINER to roles.trainer, FIELD_STUDENT to roles.student),
        )

        role?.let { fields[FIELD_ACTIVE_ROLE] = it.storageValue }
        details.displayName?.let { fields[FIELD_DISPLAY_NAME] = it }
        // Texto ISO, e não Timestamp: data de nascimento não tem hora, e um Timestamp a deslocaria
        // um dia inteiro conforme o fuso de quem lê.
        details.birthDate?.let { fields[FIELD_BIRTH_DATE] = it.toString() }
        details.phone?.let { fields[FIELD_PHONE] = it }
        // Só a sigla vai ao banco. O nome por extenso é remontado na exibição, e gravá-lo aqui
        // seria uma segunda grafia do mesmo estado esperando para divergir da primeira.
        details.state?.let { fields[FIELD_STATE] = it }
        details.city?.let { fields[FIELD_CITY] = it }
        details.consent?.let { fields[FIELD_CONSENTS] = consentFields(it) }

        return fields
    }

    /**
     * Momento do aceite vem do **servidor**, não do aparelho: prova de consentimento carimbada por
     * um relógio que o titular pode alterar não prova nada.
     */
    fun consentFields(consent: PrivacyConsent): Map<String, Any> = mapOf(
        FIELD_TERMS_VERSION to consent.termsVersion,
        FIELD_TERMS_ACCEPTED_AT to FieldValue.serverTimestamp(),
        FIELD_MARKETING_OPT_IN to consent.marketingOptIn,
    )

    /**
     * O mapa da edição de identidade, onde nulo **apaga**.
     *
     * É o oposto de [fields], e de propósito: ali nulo é "não informei" e o campo é omitido; aqui
     * nulo é "apaguei o número", e omitir preservaria o antigo — que é exatamente o que esta
     * gravação existe para não fazer.
     */
    fun identityFields(displayName: String, phone: String?, state: String?, city: String?): Map<String, Any> = mapOf(
        FIELD_DISPLAY_NAME to displayName,
        FIELD_PHONE to (phone ?: FieldValue.delete()),
        FIELD_STATE to (state ?: FieldValue.delete()),
        FIELD_CITY to (city ?: FieldValue.delete()),
    )

    /**
     * O perfil resultante do cadastro, montado sem reler o documento.
     *
     * Espelha [fields] campo a campo: o que não foi enviado continua sendo o que já estava lá. Se
     * divergir do mapa gravado, a tela passa a mostrar um estado que o banco não tem.
     */
    fun merged(uid: String, existing: UserProfile?, role: ActiveRole?, details: SignUpDetails) = UserProfile(
        uid = uid,
        displayName = nameToWrite(existing?.displayName, details.displayName) ?: existing?.displayName,
        roles = roles(existing?.roles, role),
        activeRole = role ?: existing?.activeRole,
        birthDate = details.birthDate ?: existing?.birthDate,
        phone = details.phone ?: existing?.phone,
        acceptedTermsVersion = details.consent?.termsVersion ?: existing?.acceptedTermsVersion,
        state = details.state ?: existing?.state,
        city = details.city ?: existing?.city,
    )
}
