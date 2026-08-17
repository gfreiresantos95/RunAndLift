package com.gabrielfreire.runandlift.data.trainer

import com.gabrielfreire.runandlift.data.model.ShowcaseConsent
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.google.firebase.firestore.FieldValue

/**
 * Como o perfil profissional vira documento — os nomes dos campos e o mapa de gravação.
 *
 * Mora fora de [FirestoreTrainerRepository] por uma razão só, e é a que importa: **aqui está a
 * regra da vitrine**, e ali estão as chamadas ao SDK. Separadas, a regra pode ser afirmada por um
 * teste comum, sem emulador e sem Firestore — e é justamente ela que ninguém pode descobrir quebrada
 * em produção, porque o que se perde ao quebrá-la é o controle do titular sobre o que fica público.
 *
 * Os nomes dos campos ficam com o mapa que os escreve, e não espalhados: `showcase.enabled` é lido
 * pela regra do Firestore, e renomeá-lo aqui tira todo perfil do ar sem mudar uma linha de regra.
 */
internal object TrainerDocument {

    const val COLLECTION = "trainerProfiles"

    /** Gravado pelo cadastro, lido pelo perfil e nunca escrito daqui — ver [TrainerRepository.save]. */
    const val FIELD_CREF = "cref"

    const val FIELD_EXPERIENCE = "experience"
    const val FIELD_SPECIALTIES = "specialties"
    const val FIELD_MODES = "serviceModes"
    const val FIELD_DAYS = "availableDays"
    const val FIELD_BIO = "bio"
    const val FIELD_MAX_STUDENTS = "maxStudents"
    const val FIELD_ONBOARDED_AT = "onboardingCompletedAt"

    /**
     * Código de convite vigente do treinador, escrito e lido por `FirestoreLinkRepository`.
     *
     * Mora neste documento porque o convite é ferramenta **dele**, e porque guardá-lo aqui é o que
     * dispensa procurar o código pelo dono: `inviteCodes` é legível por qualquer autenticado, e uma
     * consulta por `trainerId` ali transformaria "ler o código que me deram" em "listar todos os
     * códigos que existem".
     *
     * Não entra em [fields] nem em `TrainerProfile`: não é campo de formulário de perfil, e não é
     * coisa que o aluno veja ao abrir um treinador.
     */
    const val FIELD_INVITE_CODE = "inviteCode"

    const val FIELD_SHOWCASE = "showcase"
    const val FIELD_ENABLED = "enabled"
    const val FIELD_VERSION = "version"
    const val FIELD_ACCEPTED_AT = "acceptedAt"

    // Na leitura o ponto é caminho, então aqui a forma achatada está correta.
    const val FIELD_SHOWCASE_ENABLED = "$FIELD_SHOWCASE.$FIELD_ENABLED"
    const val FIELD_SHOWCASE_VERSION = "$FIELD_SHOWCASE.$FIELD_VERSION"

    /**
     * Só o que veio preenchido entra no mapa.
     *
     * `SetOptions.merge()` sobrescreve campo presente e preserva campo ausente, então mandar `null`
     * explícito apagaria dado bom — omitir é o que faz a gravação parcial ser segura.
     *
     * @param published quando falso, apresentação e capacidade não entram **mesmo vindo
     *   preenchidas**. É o ponto único onde a regra da vitrine é aplicada.
     */
    fun fields(details: TrainerProfileDetails, published: Boolean): Map<String, Any> {
        val fields = mutableMapOf<String, Any>()

        details.experience?.let { fields[FIELD_EXPERIENCE] = it.name }
        // Conjunto vazio é resposta legítima e é gravado; `null` é que significa "não mexa nisto".
        details.specialties?.let { set -> fields[FIELD_SPECIALTIES] = set.map { it.name }.sorted() }
        details.serviceModes?.let { set -> fields[FIELD_MODES] = set.map { it.name }.sorted() }
        details.availableDays?.let { days -> fields[FIELD_DAYS] = days.map { it.value }.sorted() }

        details.showcase?.let { fields[FIELD_SHOWCASE] = showcaseFields(it) }
        // Carimbado pelo servidor e nunca reescrito: é a data em que o passo a passo terminou, e o
        // relógio do aparelho não serve para responder "quando".
        if (details.onboardingDone) fields[FIELD_ONBOARDED_AT] = FieldValue.serverTimestamp()

        if (published) {
            // Texto **vazio** aqui é "apaguei a apresentação", e não "não informei": é a única
            // forma de o campo de fato sumir do banco numa tela de edição.
            details.bio?.let { fields[FIELD_BIO] = it.ifEmpty { FieldValue.delete() } }
            details.maxStudents?.let { fields[FIELD_MAX_STUDENTS] = it }
        }

        return fields
    }

    /**
     * O mapa que a regra do Firestore lê.
     *
     * A retirada grava **só** `enabled = false`: versão e momento do aceite continuam onde estão,
     * porque são o registro de que a publicação foi autorizada enquanto durou (LGPD art. 8º, §1º).
     * Apagá-los seria destruir a prova junto com a permissão.
     *
     * O momento do aceite vem do **servidor**: consentimento carimbado pelo relógio do aparelho,
     * que o titular pode alterar, não prova nada (art. 8º, §2º).
     */
    private fun showcaseFields(consent: ShowcaseConsent): Map<String, Any> = if (consent.accepted) {
        mapOf(
            FIELD_ENABLED to true,
            FIELD_VERSION to consent.version,
            FIELD_ACCEPTED_AT to FieldValue.serverTimestamp(),
        )
    } else {
        mapOf(FIELD_ENABLED to false)
    }
}

/**
 * O perfil gravado somado ao que acabou de ser escrito.
 *
 * Mora ao lado de [TrainerDocument.fields] porque os dois **têm de concordar**: um decide o que vai
 * ao banco, o outro decide o que a chamada devolve sem reler. Em arquivos distantes, a divergência
 * entre eles apareceria como um campo que "some até reabrir a tela".
 */
internal fun TrainerProfile.mergedWith(details: TrainerProfileDetails, published: Boolean) = copy(
    experience = details.experience ?: experience,
    specialties = details.specialties ?: specialties,
    serviceModes = details.serviceModes ?: serviceModes,
    availableDays = details.availableDays ?: availableDays,
    // Espelha a gravação: ausente preserva, vazio apaga. Um `?:` simples devolveria o texto antigo
    // justamente no caso em que a pessoa acabou de apagá-lo.
    bio = if (published && details.bio != null) details.bio.takeIf { it.isNotEmpty() } else bio,
    maxStudents = details.maxStudents.takeIf { published } ?: maxStudents,
    // A versão sobrevive à retirada — é o registro do aceite, e não o estado da vitrine.
    showcaseVersion = details.showcase?.version?.takeIf { details.showcase.accepted } ?: showcaseVersion,
    showcaseEnabled = details.showcase?.accepted ?: showcaseEnabled,
    onboarded = onboarded || details.onboardingDone,
)
