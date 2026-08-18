package com.gabrielfreire.runandlift.data.student

import com.gabrielfreire.runandlift.data.model.StudentProfile
import com.gabrielfreire.runandlift.data.model.StudentProfileDetails
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * [StudentRepository] sobre o Firestore.
 *
 * Aqui ficaram só as chamadas ao SDK: o que decide — o mapa gravado, a trava do consentimento de
 * saúde e a leitura das duas listas — mora em [StudentDocument], onde um teste comum o alcança.
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
            level = TrainingLevel.fromStored(document.getString(StudentDocument.FIELD_LEVEL)),
            goal = TrainingGoal.fromStored(document.getString(StudentDocument.FIELD_GOAL)),
            availableDays = StudentDocument.days(document.get(StudentDocument.FIELD_DAYS)),
            // Peso e altura chegam como número; qualquer coisa fora disso vira ausência em vez de
            // exceção — campo corrompido não pode impedir alguém de abrir o app.
            weightKg = document.getDouble(StudentDocument.FIELD_WEIGHT),
            heightCm = document.getLong(StudentDocument.FIELD_HEIGHT)?.toInt(),
            injuries = StudentDocument.injuries(document.get(StudentDocument.FIELD_INJURIES)),
            // O texto livre do campo antigo vira a observação do novo. Quem escreveu "dói o ombro
            // direito quando levanto acima da cabeça" antes de a lista existir não pode ver isso
            // sumir porque o formato mudou — reaparece no campo "Outra", já preenchido.
            injuryNotes = document.getString(StudentDocument.FIELD_INJURY_NOTES)
                ?: document.getString(StudentDocument.FIELD_LEGACY_RESTRICTIONS),
            healthConsentVersion = document.getString(StudentDocument.FIELD_HEALTH_CONSENT_VERSION),
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

            document(uid).set(StudentDocument.fields(details, consented), SetOptions.merge()).await()

            // Devolve o estado resultante em vez de reler: uma segunda leitura custaria do
            // orçamento para responder o que esta função acabou de escrever.
            StudentDocument.merged(stored ?: StudentProfile(uid = uid), details, consented)
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

    private fun document(uid: String) = firestore.collection(StudentDocument.COLLECTION).document(uid)
}
