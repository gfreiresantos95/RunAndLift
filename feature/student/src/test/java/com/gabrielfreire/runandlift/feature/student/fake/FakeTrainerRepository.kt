package com.gabrielfreire.runandlift.feature.student.fake

import com.gabrielfreire.runandlift.data.model.TrainerProfile
import com.gabrielfreire.runandlift.data.model.TrainerProfileDetails
import com.gabrielfreire.runandlift.data.trainer.TrainerRepository

/**
 * [TrainerRepository] de mentira, escrito à mão — o projeto não usa MockK por decisão.
 *
 * **Não é cópia da do `:feature:trainer`**, e a diferença é o papel: o aluno só **lê** este
 * documento, e o que ele lê dali é uma coisa só, o registro no CREF de quem o treina. Gravar falha
 * em voz alta — uma tela de aluno que escrevesse no perfil profissional de outra pessoa é erro de
 * arquitetura, e a regra do Firestore (`allow write: if isSelf(uid)`) a recusaria de qualquer forma.
 *
 * @param profile o perfil que a leitura encontra. `null` é o treinador cujo documento não existe —
 *   raro, mas possível em conta que virou treinador por outro caminho.
 * @param failReading leitura que não responde. É o caso que a home precisa saber atravessar: sem o
 *   registro, o nome do treinador continua na tela.
 */
internal class FakeTrainerRepository(
    private val profile: TrainerProfile? = TrainerProfile(uid = TRAINER_ID, cref = CREF),
    var failReading: Boolean = false,
) : TrainerRepository {

    var readCount: Int = 0
        private set

    override suspend fun profile(uid: String): TrainerProfile? {
        readCount++

        if (failReading) error("sem rede e sem cache")

        return profile?.takeIf { it.uid == uid }
    }

    override suspend fun save(uid: String, details: TrainerProfileDetails): TrainerProfile =
        error("o aluno não escreve no perfil profissional de ninguém")

    companion object {

        /** O treinador das outras fixtures deste módulo, para os ids baterem entre os fakes. */
        const val TRAINER_ID = "t1"

        /** Categoria `G`, que é a de quem pode prescrever exercício (Lei 9.696/1998). */
        const val CREF = "012345-G/SP"
    }
}
