package com.gabrielfreire.runandlift.data.model

/**
 * Um exercício **prescrito**: o movimento mais o que fazer com ele.
 *
 * A diferença entre isto e [Exercise] é a diferença entre o catálogo e o treino. "Supino reto" é um
 * exercício; "supino reto, 4 séries de 8 a 12, 60 kg, 90 s de descanso" é uma prescrição, e é ela
 * que o aluno lê na academia.
 *
 * **O nome do exercício viaja junto do id**, como acontece em [Link] com os nomes das duas pessoas.
 * A razão é a mesma: a tela que desenha o treino não pode depender de uma segunda fonte para
 * escrever uma linha. Aqui há um motivo a mais — o catálogo é republicado de fora do app, e um
 * exercício que saia dele não pode transformar um treino prescrito em uma lista de identificadores.
 *
 * @param sets quantas séries. É o único número sempre obrigatório: um exercício sem séries não foi
 *   prescrito, foi só escolhido.
 * @param minReps e @param maxReps a faixa de repetições. São dois números e não um texto porque o
 *   app precisa somar volume e, mais adiante, comparar o prescrito com o executado — coisa que
 *   "8-12" escrito à mão não permite. Quem quer número fixo põe o mesmo valor nos dois, e a tela
 *   mostra "10" em vez de "10 a 10".
 * @param loadKg carga sugerida, **opcional de propósito**. Vazio quer dizer "a combinar" ou "você já
 *   sabe a sua" — que é o caso comum de quem treina há tempo, e o caso obrigatório na primeira
 *   semana, quando o treinador ainda não conhece as cargas da pessoa.
 * @param restSeconds descanso entre séries, também opcional.
 * @param notes o recado do treinador para este exercício — "desce devagar", "não trave o cotovelo".
 *   É o que separa uma planilha de um acompanhamento.
 */
data class PrescribedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val sets: Int,
    val minReps: Int,
    val maxReps: Int,
    val loadKg: Double? = null,
    val restSeconds: Int? = null,
    val notes: String? = null,
) {

    /** Se a faixa é na verdade um número só, e a tela deve escrever "10" em vez de "10 a 10". */
    val hasFixedReps: Boolean get() = minReps == maxReps

    companion object {

        /** Séries de um exercício recém-escolhido, antes de o treinador ajustar qualquer coisa. */
        const val DEFAULT_SETS = 3

        /**
         * A faixa que o catálogo do mercado assume quando ninguém disse nada.
         *
         * 8 a 12 é a faixa de hipertrofia, que é o objetivo mais comum de quem procura treinador —
         * e um padrão que serve à maioria economiza dois toques por exercício em um formulário que
         * se preenche quarenta vezes.
         */
        const val DEFAULT_MIN_REPS = 8
        const val DEFAULT_MAX_REPS = 12

        /** Descanso padrão, em segundos. */
        const val DEFAULT_REST_SECONDS = 60

        /** Um exercício do catálogo virando prescrição, com os padrões acima. */
        fun from(exercise: Exercise): PrescribedExercise = PrescribedExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            sets = DEFAULT_SETS,
            minReps = DEFAULT_MIN_REPS,
            maxReps = DEFAULT_MAX_REPS,
            restSeconds = DEFAULT_REST_SECONDS,
        )
    }
}
