package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.TrainingGoal
import com.gabrielfreire.runandlift.data.model.TrainingLevel
import com.gabrielfreire.runandlift.feature.student.validation.HeightError
import com.gabrielfreire.runandlift.feature.student.validation.TrainingFormValidation
import com.gabrielfreire.runandlift.feature.student.validation.WeightError
import java.time.DayOfWeek

/**
 * O formulário de perfil de treino — **os mesmos campos no onboarding e na edição**.
 *
 * O pacote existe pela mesma razão do `profileform/` do `:feature:auth`: dois fluxos compartilham o
 * conteúdo, e o nome diz quais. O onboarding mostra um campo por passo; a edição mostra todos numa
 * tela. O que se pergunta, como se valida e o que se grava é um só, escrito uma vez.
 *
 * [weight] e [height] são **texto**, e não número, porque é isso que um campo de texto tem: "1,7"
 * no meio da digitação não é um peso inválido, é um peso pela metade. A conversão acontece uma vez,
 * no caminho da gravação.
 *
 * @param healthConsent se o aluno aceitou o aviso de dado de saúde **nesta sessão do formulário**.
 *   Vem verdadeiro quando já havia aceite gravado. Enquanto for falso, peso, altura e restrições
 *   não são perguntados nem gravados — a regra é aplicada de novo no repositório, que é onde ela
 *   não depende de nenhuma tela lembrar dela.
 */
internal data class TrainingFormState(
    val level: TrainingLevel? = null,
    val goal: TrainingGoal? = null,
    val availableDays: Set<DayOfWeek> = emptySet(),
    val weight: String = "",
    val height: String = "",
    val restrictions: String = "",
    val healthConsent: Boolean = false,
    val weightError: WeightError? = null,
    val heightError: HeightError? = null,
) {

    /** Nada pendente. Consultado depois de [validated], nunca antes. */
    val isValid: Boolean get() = weightError == null && heightError == null

    /** Liga ou desliga um dia, que é o que um toque na tecla do dia significa. */
    fun toggleDay(day: DayOfWeek): TrainingFormState =
        copy(availableDays = if (day in availableDays) availableDays - day else availableDays + day)
}

/**
 * O formulário conferido.
 *
 * Só peso e altura têm o que conferir: nível, objetivo e dias são escolhas de uma lista fechada, e
 * restrições é texto livre que ninguém pode errar. Campo vazio continua válido — todos são
 * opcionais, e o onboarding deixa pular.
 */
internal fun TrainingFormState.validated(): TrainingFormState = copy(
    weightError = TrainingFormValidation.validateWeight(weight),
    heightError = TrainingFormValidation.validateHeight(height),
)
