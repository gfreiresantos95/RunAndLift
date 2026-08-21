package com.gabrielfreire.runandlift.data.assignment

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.AssignmentStatus
import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.program.ProgramDays

/**
 * O mapa que vai e volta do Firestore, separado da conversa com ele.
 *
 * Mesma divisão de `ProgramDocument` e `ExerciseDocument`, pela mesma razão: aqui está a decisão do
 * que é gravado e do que é descartado, e um teste comum de JVM alcança tudo sem emulador.
 *
 * **Os dias são serializados pelo mesmo código do programa** ([ProgramDays]). Não é economia de
 * linhas: a cópia congelada tem de ser byte a byte a mesma estrutura do molde, e duas rotinas de
 * gravação para a mesma forma divergiriam no dia em que um campo novo entrasse em uma delas.
 */
internal object AssignmentDocument {

    const val COLLECTION = "assignments"

    const val FIELD_TRAINER_ID = "trainerId"
    const val FIELD_STUDENT_ID = "studentId"
    const val FIELD_STUDENT_NAME = "studentName"
    const val FIELD_PROGRAM_ID = "programId"
    const val FIELD_PROGRAM_NAME = "programName"
    const val FIELD_GOAL = "goal"
    const val FIELD_NOTES = "notes"
    const val FIELD_DAYS = "days"
    const val FIELD_STATUS = "status"
    const val FIELD_UPDATED_AT = "updatedAt"

    /** O mapa a ser gravado. `updatedAt` fica de fora: quem o escreve é o repositório, com o relógio do servidor. */
    fun toMap(assignment: Assignment): Map<String, Any?> = mapOf(
        FIELD_TRAINER_ID to assignment.trainerId,
        FIELD_STUDENT_ID to assignment.studentId,
        FIELD_STUDENT_NAME to assignment.studentName,
        FIELD_PROGRAM_ID to assignment.programId,
        FIELD_PROGRAM_NAME to assignment.programName,
        FIELD_GOAL to assignment.goal?.name,
        FIELD_NOTES to assignment.notes?.trim()?.takeIf { it.isNotEmpty() },
        FIELD_DAYS to assignment.days.map(ProgramDays::toMap),
        FIELD_STATUS to assignment.status.stored,
    )

    /**
     * O documento virando [Assignment], ou `null` quando falta o que o torna uma prescrição.
     *
     * Sem treinador, sem aluno ou sem programa não há o que mostrar de nenhum dos dois lados. Um dia
     * quebrado lá dentro, ao contrário, some sozinho — a mesma política do programa: o treino do
     * aluno não pode sumir inteiro por causa de um item mal formado.
     */
    fun assignment(data: Map<String, Any?>?): Assignment? {
        val trainerId = data?.get(FIELD_TRAINER_ID) as? String
        val studentId = data?.get(FIELD_STUDENT_ID) as? String
        val programId = data?.get(FIELD_PROGRAM_ID) as? String
        if (trainerId == null || studentId == null || programId == null) return null

        return Assignment(
            trainerId = trainerId,
            studentId = studentId,
            studentName = data[FIELD_STUDENT_NAME] as? String ?: "",
            programId = programId,
            programName = data[FIELD_PROGRAM_NAME] as? String ?: "",
            goal = TrainingGoal.fromStored(data[FIELD_GOAL] as? String),
            notes = data[FIELD_NOTES] as? String,
            days = (data[FIELD_DAYS] as? List<*>).orEmpty().mapNotNull(ProgramDays::day),
            status = AssignmentStatus.fromStored(data[FIELD_STATUS] as? String),
            updatedAt = (data[FIELD_UPDATED_AT] as? Number)?.toLong() ?: 0L,
        )
    }
}
