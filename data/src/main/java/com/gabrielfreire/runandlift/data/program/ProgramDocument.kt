package com.gabrielfreire.runandlift.data.program

import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.TrainingGoal

/**
 * O mapa que vai e volta do Firestore, separado da conversa com ele.
 *
 * É a mesma divisão de `TrainerDocument` e `ExerciseDocument`, pela mesma razão: aqui está a
 * **decisão** — o que é gravado, o que é descartado, o que acontece com um documento malformado — e
 * um teste comum de JVM alcança tudo isso sem emulador. Em [FirestoreProgramRepository] ficam só as
 * chamadas do SDK.
 *
 * Os dias ficam em [ProgramDays], porque `assignments` grava exatamente a mesma forma.
 *
 * Duas políticas guiam a leitura, e as duas vêm de já ter apanhado disto em outra coleção:
 *
 * - **Documento sem `trainerId` ou sem `name` é descartado**, porque não há como mostrá-lo nem como
 *   saber de quem é.
 * - **Dentro dele, o que estiver estranho some sozinho** em vez de derrubar o programa inteiro.
 */
internal object ProgramDocument {

    const val COLLECTION = "programs"

    const val FIELD_TRAINER_ID = "trainerId"
    const val FIELD_NAME = "name"
    const val FIELD_GOAL = "goal"
    const val FIELD_NOTES = "notes"
    const val FIELD_DAYS = "days"
    const val FIELD_UPDATED_AT = "updatedAt"

    /**
     * O mapa a ser gravado.
     *
     * `updatedAt` **não** entra aqui: quem o escreve é o repositório, com o carimbo do servidor, e
     * um valor vindo do relógio do aparelho serviria para ordenar errado a lista de quem está com a
     * data do celular trocada.
     */
    fun toMap(program: Program): Map<String, Any?> = mapOf(
        FIELD_TRAINER_ID to program.trainerId,
        FIELD_NAME to program.name.trim(),
        FIELD_GOAL to program.goal?.name,
        FIELD_NOTES to program.notes?.trim()?.takeIf { it.isNotEmpty() },
        FIELD_DAYS to program.days.map(ProgramDays::toMap),
    )

    /** O documento virando [Program], ou `null` quando falta o que o torna um programa. */
    fun program(id: String, data: Map<String, Any?>?): Program? {
        val trainerId = data?.get(FIELD_TRAINER_ID) as? String
        val name = data?.get(FIELD_NAME) as? String
        if (trainerId == null || name == null) return null

        return Program(
            id = id,
            trainerId = trainerId,
            name = name,
            goal = TrainingGoal.fromStored(data[FIELD_GOAL] as? String),
            notes = data[FIELD_NOTES] as? String,
            days = (data[FIELD_DAYS] as? List<*>).orEmpty().mapNotNull(ProgramDays::day),
            updatedAt = (data[FIELD_UPDATED_AT] as? Number)?.toLong() ?: 0L,
        )
    }
}
