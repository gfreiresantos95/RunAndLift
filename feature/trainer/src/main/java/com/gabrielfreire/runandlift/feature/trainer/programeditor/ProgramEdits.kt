package com.gabrielfreire.runandlift.feature.trainer.programeditor

import com.gabrielfreire.runandlift.data.model.Exercise
import com.gabrielfreire.runandlift.data.model.PrescribedExercise
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.data.model.ProgramDay

/*
 * As mudanças que se faz num programa enquanto ele é montado, como funções puras.
 *
 * Ficam aqui, e não dentro do controlador, por um motivo prático: **índice fora da lista é o erro
 * mais provável desta tela inteira**. O editor de dia recebe a posição por argumento de navegação, o
 * de prescrição recebe duas, e as duas continuam valendo depois de o usuário remover algo — o
 * processo pode até ser recriado no meio, com a rota antiga e o programa novo. Cada função abaixo
 * responde a isso devolvendo o programa **intacto** em vez de estourar, e um teste comum de JVM
 * alcança todas elas.
 *
 * Nenhuma delas grava nada. Quem grava é o repositório, quando o treinador salva.
 */

/**
 * Acrescenta um dia, já rotulado.
 *
 * O rótulo é a próxima letra do alfabeto — A, B, C — porque é assim que uma planilha de academia
 * chama os dias, e porque um dia sem nome obrigaria a preencher um campo antes de fazer qualquer
 * outra coisa. Passado o Z, volta a contar em número: quem tem 27 dias num programa não está mais
 * seguindo a convenção de qualquer forma.
 */
internal fun Program.withDayAdded(): Program {
    val label = days.size.let { index ->
        if (index < ALPHABET_SIZE) ('A' + index).toString() else (index + 1).toString()
    }
    return copy(days = days + ProgramDay(label = label))
}

/** Remove o dia da posição, ou devolve o programa como está se ela não existir. */
internal fun Program.withDayRemoved(dayIndex: Int): Program =
    if (dayIndex !in days.indices) this else copy(days = days.filterIndexed { index, _ -> index != dayIndex })

/** Troca o rótulo e o foco de um dia. Foco em branco vira ausência, e não texto vazio. */
internal fun Program.withDayInfo(dayIndex: Int, label: String, focus: String): Program =
    withDay(dayIndex) { it.copy(label = label, focus = focus.takeIf { text -> text.isNotBlank() }) }

/**
 * Acrescenta um exercício do catálogo ao fim do dia, já com a prescrição padrão.
 *
 * **Repetido é permitido**, de propósito: supino reto duas vezes no mesmo dia, com cargas
 * diferentes, é prescrição comum — e recusar isso seria o app achar que sabe mais que o
 * profissional.
 */
internal fun Program.withExerciseAdded(dayIndex: Int, exercise: Exercise): Program =
    withDay(dayIndex) { it.copy(exercises = it.exercises + PrescribedExercise.from(exercise)) }

/** Remove um exercício do dia, ou devolve o programa como está se a posição não existir. */
internal fun Program.withExerciseRemoved(dayIndex: Int, exerciseIndex: Int): Program = withDay(dayIndex) { day ->
    if (exerciseIndex !in day.exercises.indices) {
        day
    } else {
        day.copy(exercises = day.exercises.filterIndexed { index, _ -> index != exerciseIndex })
    }
}

/**
 * Move um exercício uma posição para cima ou para baixo.
 *
 * A ordem é a de execução, e ela importa: composto antes de isolado é a conta que o treinador faz
 * ao montar. Mover para fora da lista não faz nada — é o que acontece ao tocar "subir" no primeiro.
 */
internal fun Program.withExerciseMoved(dayIndex: Int, from: Int, to: Int): Program = withDay(dayIndex) { day ->
    if (from !in day.exercises.indices || to !in day.exercises.indices) {
        day
    } else {
        day.copy(exercises = day.exercises.toMutableList().apply { add(to, removeAt(from)) })
    }
}

/** Substitui a prescrição de um exercício pelo que a tela devolveu. */
internal fun Program.withPrescription(dayIndex: Int, exerciseIndex: Int, prescription: PrescribedExercise): Program =
    withDay(dayIndex) { day ->
        if (exerciseIndex !in day.exercises.indices) {
            day
        } else {
            day.copy(
                exercises = day.exercises.mapIndexed { index, current ->
                    if (index == exerciseIndex) prescription else current
                },
            )
        }
    }

/**
 * Aplica uma mudança ao dia da posição, ou devolve o programa intacto.
 *
 * É por aqui que todas as funções acima passam, e é o único lugar que precisa acertar a verificação
 * de índice do dia — as outras só cuidam do índice do exercício.
 */
private fun Program.withDay(dayIndex: Int, transform: (ProgramDay) -> ProgramDay): Program =
    if (dayIndex !in days.indices) {
        this
    } else {
        copy(days = days.mapIndexed { index, day -> if (index == dayIndex) transform(day) else day })
    }

private const val ALPHABET_SIZE = 26
