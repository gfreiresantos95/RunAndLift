package com.gabrielfreire.runandlift.feature.trainer.studentworkout

import com.gabrielfreire.runandlift.data.model.Assignment
import com.gabrielfreire.runandlift.data.model.ProgramDay

/**
 * Estado da tela que mostra ao treinador o treino que um aluno recebeu.
 *
 * **Três situações, e nenhuma delas é a outra.** Nunca prescrevi nada para esta pessoa; prescrevi e
 * depois encerrei; a leitura não respondeu. As duas primeiras dizem que não há treino, e a terceira
 * oferece tentar de novo — desenhá-las iguais mandaria o treinador prescrever de novo o que ele já
 * prescreveu, por causa de uma falha de rede. E reatribuir substitui a cópia que o aluno está
 * usando na academia.
 *
 * @param assignment o que foi prescrito, com a **cópia congelada** dos dias. É a mesma cópia que o
 *   aluno abre na academia, e é de propósito: mostrar aqui o molde em `programs` exibiria a edição
 *   mais recente dele, que pode não ser o que o aluno tem na mão. A pergunta desta tela é o que o
 *   aluno recebeu, e não o que o molde diz hoje.
 * @param failed leitura que não respondeu. Separada de "não há treino" pela razão de sempre: as duas
 *   desenham a mesma tela vazia e são duas conversas completamente diferentes.
 */
internal data class StudentWorkoutUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val assignment: Assignment? = null,
) {

    /**
     * De quem é este treino, ou vazio quando ainda não há atribuição de onde tirar o nome.
     *
     * O nome sai **de dentro do documento**, e não da rota: ele já viaja copiado ali pela mesma
     * razão que viaja dentro do vínculo — `users/{uid}` é legível só pelo titular. Levá-lo pela rota
     * exigiria codificar acento e espaço num caminho de URL para reescrever, dois passos adiante, o
     * que o documento já responde.
     */
    val studentName: String get() = assignment?.studentName.orEmpty()

    /** Nunca houve prescrição para esta pessoa — e a leitura funcionou, que é o que separa de [failed]. */
    val isEmpty: Boolean get() = assignment == null && !failed

    /**
     * A prescrição existe, mas foi encerrada.
     *
     * Estado próprio, e não vazio: o documento que fica é justamente o que permite dizer "este
     * treino foi encerrado" em vez de a tela esvaziar como se nada tivesse acontecido — que é a
     * mesma razão de `end` não apagar nada.
     */
    val isEnded: Boolean get() = assignment != null && !assignment.isActive

    /** Os dias na ordem em que o treinador os montou. A ordem é dele, e a tela não a refaz. */
    val days: List<ProgramDay> get() = assignment?.days.orEmpty()

    /** Quantos exercícios o treino tem somando todos os dias — o tamanho do que a pessoa recebeu. */
    val totalExercises: Int get() = assignment?.totalExercises ?: 0
}
