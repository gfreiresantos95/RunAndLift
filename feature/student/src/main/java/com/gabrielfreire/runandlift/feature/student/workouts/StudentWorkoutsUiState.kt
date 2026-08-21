package com.gabrielfreire.runandlift.feature.student.workouts

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.ProgramDay

/**
 * Estado da aba de treinos do aluno.
 *
 * **Vazio e falha são coisas diferentes, e aqui a diferença é maior do que nas outras telas.** "Seu
 * treinador ainda não montou seu treino" dito a quem tem treino e está sem sinal manda a pessoa
 * cobrar o treinador por algo que ele já fez — e a frase é convincente o bastante para ninguém
 * desconfiar da rede. Por isso [isEmpty] exige [failed] falso.
 *
 * @param assignment a prescrição ativa, com a **cópia congelada** dos dias. O aluno não consegue ler
 *   a coleção de programas — a regra de `programs` é `isSelf(trainerId)` —, então o que está aqui é
 *   tudo o que ele tem do treino, e é de propósito: ver `Assignment`.
 * @param failed a leitura não respondeu. Oferece tentar de novo; o vazio não tem o que oferecer,
 *   porque quem monta o treino é outra pessoa.
 */
internal data class StudentWorkoutsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val assignment: Assignment? = null,
) {

    /** Não há treino prescrito — e a leitura funcionou, que é o que separa isto de [failed]. */
    val isEmpty: Boolean get() = assignment == null && !failed

    /** Os dias na ordem em que o treinador os montou. A ordem é dele, e a tela não a refaz. */
    val days: List<ProgramDay> get() = assignment?.days.orEmpty()

    /**
     * Um dia pela posição, ou `null` se ela não existir mais.
     *
     * Nulo acontece de verdade: a posição vem por argumento de navegação e continua valendo depois
     * de o treinador reatribuir um programa mais curto — o processo pode até ser recriado no meio,
     * com a rota antiga e a prescrição nova. Devolver nulo é o que faz a tela dizer isso em vez de
     * estourar, que é a mesma decisão de `ProgramEdits` do outro lado.
     */
    fun day(index: Int): ProgramDay? = days.getOrNull(index)
}
