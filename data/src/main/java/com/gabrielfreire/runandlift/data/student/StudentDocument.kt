package com.gabrielfreire.runandlift.data.student

import com.gabrielfreire.runandlift.data.model.HealthDataConsent
import com.gabrielfreire.runandlift.data.model.InjuryArea
import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.google.firebase.firestore.FieldValue
import java.time.DayOfWeek

/**
 * Como o perfil de treino vira documento — os nomes dos campos, o mapa de gravação e a leitura dos
 * dois campos que são lista.
 *
 * Mora fora de [FirestoreStudentRepository] pela mesma razão de `TrainerDocument` e `LinkDocument`:
 * **aqui está a regra, e lá estão as chamadas ao SDK**. E a regra que está aqui é a de maior peso do
 * app inteiro — [fields] é o ponto único onde o consentimento de dado de saúde é aplicado (LGPD art.
 * 11). Enquanto ela vivia dentro do adaptador, nenhum teste de JVM a alcançava, e o que a garantia
 * era a leitura do código.
 *
 * O que ficou de fora é só o que depende de `DocumentSnapshot`: ler campo do documento é chamada ao
 * SDK, e não decisão. As duas decisões da leitura — dia inválido some, e lesão **ausente** não é
 * lesão **vazia** — chegam aqui como `Any?`, que é o que o snapshot devolve.
 */
internal object StudentDocument {

    const val COLLECTION = "students"

    const val FIELD_LEVEL = "level"
    const val FIELD_GOAL = "goal"
    const val FIELD_DAYS = "availableDays"
    const val FIELD_WEIGHT = "weightKg"
    const val FIELD_HEIGHT = "heightCm"
    const val FIELD_INJURIES = "injuries"
    const val FIELD_INJURY_NOTES = "injuryNotes"

    /**
     * O texto livre que existia antes de a lista de regiões existir.
     *
     * Continua sendo **lido** como queda de [FIELD_INJURY_NOTES], para não sumir com o que já foi
     * escrito, e é apagado na primeira gravação que passa pelo campo novo.
     */
    const val FIELD_LEGACY_RESTRICTIONS = "restrictions"

    const val FIELD_HEALTH_CONSENT = "healthConsent"
    const val FIELD_VERSION = "version"
    const val FIELD_ACCEPTED_AT = "acceptedAt"

    // Na leitura o ponto é caminho, então aqui a forma achatada está correta.
    const val FIELD_HEALTH_CONSENT_VERSION = "$FIELD_HEALTH_CONSENT.$FIELD_VERSION"

    /**
     * Só o que veio preenchido entra no mapa.
     *
     * `SetOptions.merge()` sobrescreve campo presente e preserva campo ausente, então mandar `null`
     * explícito apagaria dado bom — omitir é o que faz a gravação parcial ser segura.
     *
     * @param consented quando falso, os três campos de saúde não entram **mesmo vindo preenchidos**.
     *   É o ponto único onde a regra do consentimento é aplicada.
     */
    fun fields(details: StudentProfileDetails, consented: Boolean): Map<String, Any> {
        val fields = mutableMapOf<String, Any>()

        details.level?.let { fields[FIELD_LEVEL] = it.name }
        details.goal?.let { fields[FIELD_GOAL] = it.name }
        // Conjunto vazio é resposta legítima e é gravado; `null` é que significa "não mexa nisto".
        details.availableDays?.let { days -> fields[FIELD_DAYS] = days.map { it.value }.sorted() }

        details.healthConsent?.let { fields[FIELD_HEALTH_CONSENT] = healthConsentFields(it) }

        if (consented) fields += healthFields(details)

        return fields
    }

    /**
     * Os três campos de saúde, montados à parte para [fields] caber na trava e não em torno dela.
     *
     * Separados, a condição do consentimento é uma linha só — e uma linha é o que se lê inteira
     * antes de mexer.
     */
    private fun healthFields(details: StudentProfileDetails): Map<String, Any> {
        val fields = mutableMapOf<String, Any>()

        details.weightKg?.let { fields[FIELD_WEIGHT] = it }
        details.heightCm?.let { fields[FIELD_HEIGHT] = it }
        // Lista vazia é resposta ("não tenho lesão") e é gravada; `null` significa "não mexa".
        details.injuries?.let { areas -> fields[FIELD_INJURIES] = areas.map { it.name }.sorted() }
        // Texto **vazio** aqui é "apaguei a observação", e não "não informei": é a única forma de
        // desmarcar "Outra" numa tela de edição e o texto de fato ir embora. Ausente continua
        // querendo dizer "não mexa nisto", como em todo o resto deste mapa.
        details.injuryNotes?.let { fields[FIELD_INJURY_NOTES] = it.ifEmpty { FieldValue.delete() } }

        // O campo antigo é apagado na primeira gravação que passa por aqui. Sem isso ele
        // sobreviveria em silêncio e voltaria a ser lido no dia em que a observação nova fosse
        // esvaziada — o texto que a pessoa apagou reaparecendo sozinho.
        if (details.injuries != null || details.injuryNotes != null) {
            fields[FIELD_LEGACY_RESTRICTIONS] = FieldValue.delete()
        }

        return fields
    }

    /**
     * Momento do aceite vem do **servidor**: prova de consentimento carimbada pelo relógio do
     * aparelho, que o titular pode alterar, não prova nada (LGPD art. 8º, §2º).
     */
    fun healthConsentFields(consent: HealthDataConsent): Map<String, Any> = mapOf(
        FIELD_VERSION to consent.version,
        FIELD_ACCEPTED_AT to FieldValue.serverTimestamp(),
    )

    /**
     * Os dias gravados virando [DayOfWeek], pelo **número ISO** (1 = segunda … 7 = domingo).
     *
     * É a forma que ordena sozinha na consulta e que não muda se a biblioteca de data mudar. Número
     * fora de 1..7 some em vez de explodir: documento escrito errado por uma versão futura não pode
     * impedir alguém de abrir o app.
     */
    fun days(stored: Any?): Set<DayOfWeek> = (stored as? List<*>)
        ?.mapNotNull { (it as? Number)?.toInt() }
        ?.mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }
        ?.toSet()
        .orEmpty()

    /**
     * As regiões lesionadas, ou `null` quando o campo **não existe** no documento.
     *
     * A diferença entre ausente e vazio é o dado: ausente é "não respondeu", vazio é "respondeu que
     * não tem nenhuma". Por isso não há queda para `emptySet()` no fim — é exatamente ela que
     * transformaria as duas coisas na mesma.
     */
    fun injuries(stored: Any?): Set<InjuryArea>? = (stored as? List<*>)
        ?.mapNotNull { InjuryArea.fromStored(it as? String) }
        ?.toSet()

    /**
     * O perfil gravado somado ao que acabou de ser escrito.
     *
     * É a regra de **gravação parcial** — nulo preserva, valor substitui, saúde só entra com
     * consentimento — e ela pertence a quem escreve, não ao tipo que só carrega dado. Existe para o
     * repositório devolver o resultado sem reler: uma segunda leitura custaria do orçamento para
     * responder o que a gravação acabou de escrever.
     */
    fun merged(profile: StudentProfile, details: StudentProfileDetails, consented: Boolean): StudentProfile =
        profile.copy(
            level = details.level ?: profile.level,
            goal = details.goal ?: profile.goal,
            availableDays = details.availableDays ?: profile.availableDays,
            weightKg = details.weightKg.takeIf { consented } ?: profile.weightKg,
            heightCm = details.heightCm.takeIf { consented } ?: profile.heightCm,
            injuries = details.injuries.takeIf { consented } ?: profile.injuries,
            // Espelha a gravação: ausente preserva, vazio apaga. Um `?:` simples devolveria o texto
            // antigo justamente no caso em que a pessoa acabou de apagá-lo.
            injuryNotes = if (consented && details.injuryNotes != null) {
                details.injuryNotes.takeIf { it.isNotEmpty() }
            } else {
                profile.injuryNotes
            },
            healthConsentVersion = details.healthConsent?.version ?: profile.healthConsentVersion,
        )
}
