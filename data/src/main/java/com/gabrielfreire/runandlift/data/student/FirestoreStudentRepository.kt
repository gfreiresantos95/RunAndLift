package com.gabrielfreire.runandlift.data.student

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.DayOfWeek

/**
 * [StudentRepository] sobre o Firestore.
 *
 * Os dias da semana são gravados pelo **número ISO** (1 = segunda … 7 = domingo), e não pelo nome
 * do enum: é a forma que ordena sozinha na consulta e que não muda se a biblioteca de data mudar.
 */
internal class FirestoreStudentRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : StudentRepository {

    override suspend fun profile(uid: String): StudentProfile? = withContext(dispatchers.io) {
        val document = readCacheFirst(document(uid))

        if (!document.exists()) return@withContext null

        StudentProfile(
            uid = uid,
            level = TrainingLevel.fromStored(document.getString(FIELD_LEVEL)),
            goal = TrainingGoal.fromStored(document.getString(FIELD_GOAL)),
            availableDays = document.readDays(),
            // Peso e altura chegam como número; qualquer coisa fora disso vira ausência em vez de
            // exceção — campo corrompido não pode impedir alguém de abrir o app.
            weightKg = document.getDouble(FIELD_WEIGHT),
            heightCm = document.getLong(FIELD_HEIGHT)?.toInt(),
            restrictions = document.getString(FIELD_RESTRICTIONS),
            healthConsentVersion = document.getString(FIELD_HEALTH_CONSENT_VERSION),
        )
    }

    override suspend fun save(uid: String, details: StudentProfileDetails): StudentProfile =
        withContext(dispatchers.io) {
            // O consentimento que vale é o que **já está gravado** ou o que vem nesta mesma
            // escrita. Ler antes custa 0 com o cache quente, e é o que permite o passo de peso e
            // altura ser gravado numa chamada separada da do aceite, sem que a segunda perca a
            // autorização dada na primeira.
            val stored = profile(uid)
            val consented = details.healthConsent != null || stored?.hasHealthConsent == true

            document(uid).set(fieldsFor(details, consented), SetOptions.merge()).await()

            // Devolve o estado resultante em vez de reler: uma segunda leitura custaria do
            // orçamento para responder o que esta função acabou de escrever.
            (stored ?: StudentProfile(uid = uid)).mergedWith(details, consented)
        }

    /**
     * Só o que veio preenchido entra no mapa.
     *
     * `SetOptions.merge()` sobrescreve campo presente e preserva campo ausente, então mandar `null`
     * explícito apagaria dado bom — omitir é o que faz a gravação parcial ser segura.
     *
     * @param consented quando falso, os três campos de saúde não entram **mesmo vindo
     *   preenchidos**. É o ponto único onde a regra do consentimento é aplicada.
     */
    private fun fieldsFor(details: StudentProfileDetails, consented: Boolean): Map<String, Any> {
        val fields = mutableMapOf<String, Any>()

        details.level?.let { fields[FIELD_LEVEL] = it.name }
        details.goal?.let { fields[FIELD_GOAL] = it.name }
        // Conjunto vazio é resposta legítima e é gravado; `null` é que significa "não mexa nisto".
        details.availableDays?.let { days -> fields[FIELD_DAYS] = days.map { it.value }.sorted() }

        details.healthConsent?.let { fields[FIELD_HEALTH_CONSENT] = healthConsentFields(it) }

        if (consented) {
            details.weightKg?.let { fields[FIELD_WEIGHT] = it }
            details.heightCm?.let { fields[FIELD_HEIGHT] = it }
            details.restrictions?.let { fields[FIELD_RESTRICTIONS] = it }
        }

        return fields
    }

    /**
     * Momento do aceite vem do **servidor**: prova de consentimento carimbada pelo relógio do
     * aparelho, que o titular pode alterar, não prova nada (LGPD art. 8º, §2º).
     */
    private fun healthConsentFields(consent: HealthDataConsent): Map<String, Any> = mapOf(
        FIELD_VERSION to consent.version,
        FIELD_ACCEPTED_AT to FieldValue.serverTimestamp(),
    )

    private fun DocumentSnapshot.readDays(): Set<DayOfWeek> {
        val stored = get(FIELD_DAYS) as? List<*> ?: return emptySet()

        return stored
            .mapNotNull { (it as? Number)?.toInt() }
            .mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }
            .toSet()
    }

    /**
     * Cache primeiro, servidor só quando não há nada em disco — regra 3 do orçamento (§2.4).
     *
     * A falha do cache é engolida porque "não tem em disco" não é erro; a do servidor chega a quem
     * chamou, porque sem rede e sem cache a resposta honesta é "não sei".
     */
    private suspend fun readCacheFirst(reference: DocumentReference): DocumentSnapshot =
        runCatching { reference.get(Source.CACHE).await() }
            .getOrNull()
            ?.takeIf { it.exists() }
            ?: reference.get(Source.SERVER).await()

    private fun document(uid: String) = firestore.collection(COLLECTION).document(uid)

    private companion object {
        const val COLLECTION = "students"
        const val FIELD_LEVEL = "level"
        const val FIELD_GOAL = "goal"
        const val FIELD_DAYS = "availableDays"
        const val FIELD_WEIGHT = "weightKg"
        const val FIELD_HEIGHT = "heightCm"
        const val FIELD_RESTRICTIONS = "restrictions"
        const val FIELD_HEALTH_CONSENT = "healthConsent"
        const val FIELD_VERSION = "version"
        const val FIELD_ACCEPTED_AT = "acceptedAt"

        // Na leitura o ponto é caminho, então aqui a forma achatada está correta.
        const val FIELD_HEALTH_CONSENT_VERSION = "$FIELD_HEALTH_CONSENT.$FIELD_VERSION"
    }
}

/**
 * O perfil gravado somado ao que acabou de ser escrito.
 *
 * Extensão privada do arquivo, e não método do modelo: é a regra de **gravação parcial** — nulo
 * preserva, valor substitui, saúde só entra com consentimento —, e ela pertence a quem escreve, não
 * ao tipo que só carrega dado.
 */
private fun StudentProfile.mergedWith(details: StudentProfileDetails, consented: Boolean) = copy(
    level = details.level ?: level,
    goal = details.goal ?: goal,
    availableDays = details.availableDays ?: availableDays,
    weightKg = details.weightKg.takeIf { consented } ?: weightKg,
    heightCm = details.heightCm.takeIf { consented } ?: heightCm,
    restrictions = details.restrictions.takeIf { consented } ?: restrictions,
    healthConsentVersion = details.healthConsent?.version ?: healthConsentVersion,
)
