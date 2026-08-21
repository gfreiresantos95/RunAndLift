package com.gabrielfreire.runandlift.data.program

import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.util.AppDispatchers
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * [ProgramRepository] sobre o Firestore.
 *
 * O que ficou aqui é **conversa com o SDK**: consultar, ler, escrever, apagar. O mapa de cada
 * documento — e o que acontece com um que chegue quebrado — mora em [ProgramDocument], onde um
 * teste comum de JVM o alcança sem emulador.
 *
 * A consulta filtra por `trainerId` sempre, e isso não é desempenho: é o que a regra de `programs`
 * consegue autorizar. Uma consulta sem esse filtro pediria os programas de outros treinadores, e o
 * Firestore recusa a consulta inteira em vez de devolver metade.
 */
internal class FirestoreProgramRepository(
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers,
) : ProgramRepository {

    private val collection get() = firestore.collection(ProgramDocument.COLLECTION)

    override suspend fun programs(trainerId: String): List<Program> = withContext(dispatchers.io) {
        collection
            .whereEqualTo(ProgramDocument.FIELD_TRAINER_ID, trainerId)
            .limit(ProgramRepository.LIMIT)
            .get()
            .await()
            .documents
            .mapNotNull { ProgramDocument.program(id = it.id, data = it.plainData()) }
            .sortedByDescending { it.updatedAt }
    }

    override suspend fun program(programId: String): Program? = withContext(dispatchers.io) {
        val snapshot = collection.document(programId).get().await()
        ProgramDocument.program(id = snapshot.id, data = snapshot.plainData())
    }

    /**
     * O documento com o carimbo de tempo já em milissegundos.
     *
     * Existe porque `FieldValue.serverTimestamp()` volta como `Timestamp` do Firebase, e
     * [ProgramDocument] não conhece — nem deve conhecer — tipo nenhum do SDK: é o que permite
     * afirmá-lo num teste comum de JVM. A conversão é a fronteira, e a fronteira é aqui.
     *
     * Nulo acontece de verdade: entre a escrita local e a confirmação do servidor, o campo chega
     * vazio no snapshot vindo do cache. Vira zero, e o programa recém-salvo aparece no fim da
     * ordenação até a confirmação chegar — o que é melhor do que sumir da lista.
     */
    private fun DocumentSnapshot.plainData(): Map<String, Any?>? = data?.plus(
        ProgramDocument.FIELD_UPDATED_AT to getTimestamp(ProgramDocument.FIELD_UPDATED_AT)?.toDate()?.time,
    )

    /**
     * Grava e devolve o programa com o id que ele passou a ter.
     *
     * `document()` sem argumento sorteia o id **no cliente**, sem ida à rede — é o que permite
     * devolver o programa salvo sem uma leitura a mais. O carimbo de tempo é o do **servidor**, pela
     * razão do KDoc de [ProgramDocument.toMap]: ordenar por relógio de aparelho põe na frente quem
     * está com a data trocada.
     *
     * `updatedAt` volta como estava em memória, e não como o servidor gravou. A diferença só
     * apareceria na ordenação da lista, que é relida ao voltar para ela.
     */
    override suspend fun save(program: Program): Program = withContext(dispatchers.io) {
        val document = program.id
            .takeIf { it.isNotBlank() }
            ?.let { collection.document(it) }
            ?: collection.document()

        val fields = ProgramDocument.toMap(program) +
            (ProgramDocument.FIELD_UPDATED_AT to FieldValue.serverTimestamp())

        document.set(fields).await()
        program.copy(id = document.id)
    }

    override suspend fun delete(programId: String) = withContext(dispatchers.io) {
        collection.document(programId).delete().await()
        Unit
    }
}
