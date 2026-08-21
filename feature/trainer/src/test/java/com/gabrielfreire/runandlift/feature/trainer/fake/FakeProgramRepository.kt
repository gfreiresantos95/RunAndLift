package com.gabrielfreire.runandlift.feature.trainer.fake

import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.program.ProgramRepository

/**
 * [ProgramRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * Guarda os programas **em memória e mutáveis**, pela mesma razão do [FakeLinkRepository]: a aba de
 * treinos relê a lista a cada volta do editor, e um fake que devolvesse sempre a mesma coisa faria
 * todo teste de excluir passar sem que excluir mudasse nada.
 *
 * [failReading] e [failWriting] são `var` porque a rede cai **no meio** de uma sessão — o caso que
 * interessa não é abrir a aba sem sinal, é perder o sinal com a lista já na tela.
 *
 * @param failReading leitura que não responde, sem rede e sem cache.
 * @param failWriting gravação que não completou. É a situação normal de quem tenta salvar sem sinal:
 *   a fila durável (E0-04) ainda não existe.
 */
internal class FakeProgramRepository(
    programs: List<Program> = emptyList(),
    var failReading: Boolean = false,
    var failWriting: Boolean = false,
) : ProgramRepository {

    private val programs = programs.toMutableList()

    var saved: Program? = null
        private set

    var saveCount: Int = 0
        private set

    var deleted: String? = null
        private set

    override suspend fun programs(trainerId: String): List<Program> {
        if (failReading) error("sem rede e sem cache")

        return programs.filter { it.trainerId == trainerId }
    }

    override suspend fun program(programId: String): Program? {
        if (failReading) error("sem rede e sem cache")

        return programs.firstOrNull { it.id == programId }
    }

    /**
     * Grava e devolve o programa com o id que ele passou a ter.
     *
     * O id sorteado é fixo de propósito: é o que permite afirmar que o rascunho voltou com ele em
     * memória, que é o que impede um segundo toque em salvar de criar um segundo documento.
     */
    override suspend fun save(program: Program): Program {
        if (failWriting) error("gravação não completou")

        val stored = if (program.id.isBlank()) program.copy(id = NEW_ID) else program

        programs.removeAll { it.id == stored.id }
        programs += stored
        saveCount++
        saved = stored

        return stored
    }

    override suspend fun delete(programId: String) {
        if (failWriting) error("gravação não completou")

        deleted = programId
        programs.removeAll { it.id == programId }
    }

    companion object {

        /** O id que um programa novo ganha na escrita, como o `document()` do Firestore sorteia. */
        const val NEW_ID = "p-novo"

        /** Um molde com um dia e um exercício, que é o mínimo para ele ser atribuível. */
        fun program(id: String = "p1", name: String = "Treino ABC", trainerId: String = "u1") = Program(
            id = id,
            trainerId = trainerId,
            name = name,
            days = listOf(FakeExerciseRepository.day()),
        )
    }
}
