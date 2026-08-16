package com.gabrielfreire.runandlift.data.trainer

import com.gabrielfreire.runandlift.data.model.ServiceMode
import com.gabrielfreire.runandlift.data.model.TrainerExperience
import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainerSpecialty
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.DayOfWeek

/**
 * [TrainerRepository] sobre o Firestore.
 *
 * O que sobrou aqui é **conversa com o SDK**: ler cache antes de servidor, montar o documento a
 * partir do snapshot, e escrever. As regras — o que entra no mapa, e o que a vitrine deixa entrar —
 * moram em [TrainerDocument], que é testável sem emulador.
 *
 * Os dias da semana são gravados pelo **número ISO** (1 = segunda … 7 = domingo), como no perfil do
 * aluno: é a forma que ordena sozinha na consulta e que não muda se a biblioteca de data mudar.
 */
internal class FirestoreTrainerRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : TrainerRepository {

    override suspend fun profile(uid: String): TrainerProfile? = withContext(dispatchers.io) {
        val document = readCacheFirst(document(uid))

        if (!document.exists()) return@withContext null

        TrainerProfile(
            uid = uid,
            cref = document.getString(TrainerDocument.FIELD_CREF),
            experience = TrainerExperience.fromStored(document.getString(TrainerDocument.FIELD_EXPERIENCE)),
            specialties = document.readSpecialties(),
            serviceModes = document.readModes(),
            availableDays = document.readDays(),
            bio = document.getString(TrainerDocument.FIELD_BIO),
            // Campo corrompido vira ausência em vez de exceção — não pode impedir alguém de abrir
            // o app, como no perfil do aluno.
            maxStudents = document.getLong(TrainerDocument.FIELD_MAX_STUDENTS)?.toInt(),
            showcaseVersion = document.getString(TrainerDocument.FIELD_SHOWCASE_VERSION),
            showcaseEnabled = document.getBoolean(TrainerDocument.FIELD_SHOWCASE_ENABLED) ?: false,
            onboarded = document.get(TrainerDocument.FIELD_ONBOARDED_AT) != null,
        )
    }

    override suspend fun save(uid: String, details: TrainerProfileDetails): TrainerProfile =
        withContext(dispatchers.io) {
            // O aceite que vale é o que **já está gravado** ou o que vem nesta mesma escrita — e a
            // retirada vinda nesta escrita derruba o que estava gravado. Ler antes custa 0 com o
            // cache quente, e é o que permite a apresentação ser gravada numa chamada separada da
            // do aceite sem perder a autorização dada na primeira.
            val stored = profile(uid)
            val published = details.showcase?.accepted ?: (stored?.hasShowcaseConsent == true)

            document(uid).set(TrainerDocument.fields(details, published), SetOptions.merge()).await()

            // Devolve o estado resultante em vez de reler: uma segunda leitura custaria do
            // orçamento para responder o que esta função acabou de escrever.
            (stored ?: TrainerProfile(uid = uid)).mergedWith(details, published)
        }

    private fun DocumentSnapshot.readDays(): Set<DayOfWeek> = (get(TrainerDocument.FIELD_DAYS) as? List<*>)
        .orEmpty()
        .mapNotNull { (it as? Number)?.toInt() }
        .mapNotNull { runCatching { DayOfWeek.of(it) }.getOrNull() }
        .toSet()

    private fun DocumentSnapshot.readSpecialties(): Set<TrainerSpecialty> =
        (get(TrainerDocument.FIELD_SPECIALTIES) as? List<*>)
            .orEmpty()
            .mapNotNull { TrainerSpecialty.fromStored(it as? String) }
            .toSet()

    private fun DocumentSnapshot.readModes(): Set<ServiceMode> = (get(TrainerDocument.FIELD_MODES) as? List<*>)
        .orEmpty()
        .mapNotNull { ServiceMode.fromStored(it as? String) }
        .toSet()

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

    private fun document(uid: String) = firestore.collection(TrainerDocument.COLLECTION).document(uid)
}
