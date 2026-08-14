package com.gabrielfreire.runandlift.feature.student.trainingform

import com.gabrielfreire.runandlift.data.model.InjuryArea
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
 * A pergunta de lesões ocupa **três campos** porque tem três respostas independentes: quais regiões,
 * "nenhuma", e o texto de "Outra". Um `Set` sozinho não distingue "ainda não respondi" de "não tenho
 * nada", e é justamente essa diferença que decide se o aviso da home some.
 *
 * @param healthConsent se o aluno aceitou o aviso de dado de saúde **nesta sessão do formulário**.
 *   Vem verdadeiro quando já havia aceite gravado. Enquanto for falso, peso, altura e lesões não são
 *   perguntados nem gravados — a regra é aplicada de novo no repositório, que é onde ela não depende
 *   de nenhuma tela lembrar dela.
 * @param noInjuries a pessoa declarou não ter lesão nenhuma. É **resposta**, e não ausência dela.
 * @param otherInjury o chip "Outra" está marcado, e por isso o campo de texto aparece. Existe
 *   separado de [injuryNotes] para o campo poder ficar visível e vazio — desmarcar o chip é o que
 *   apaga o texto, e não o contrário.
 */
internal data class TrainingFormState(
    val level: TrainingLevel? = null,
    val goal: TrainingGoal? = null,
    val availableDays: Set<DayOfWeek> = emptySet(),
    val weight: String = "",
    val height: String = "",
    val injuries: Set<InjuryArea> = emptySet(),
    val noInjuries: Boolean = false,
    val otherInjury: Boolean = false,
    val injuryNotes: String = "",
    val healthConsent: Boolean = false,
    val weightError: WeightError? = null,
    val heightError: HeightError? = null,
) {

    /** Nada pendente. Consultado depois de [validated], nunca antes. */
    val isValid: Boolean get() = weightError == null && heightError == null

    /**
     * Se a pergunta de lesões chegou a ser respondida — de qualquer forma, inclusive "nenhuma".
     *
     * "Outra" marcada sem texto **não** conta: um chip aceso e um campo em branco é uma resposta
     * começada, não uma resposta dada.
     */
    val injuriesAnswered: Boolean
        get() = noInjuries || injuries.isNotEmpty() || injuryNotes.isNotBlank()

    /** Liga ou desliga um dia, que é o que um toque na tecla do dia significa. */
    fun toggleDay(day: DayOfWeek): TrainingFormState =
        copy(availableDays = if (day in availableDays) availableDays - day else availableDays + day)

    /**
     * Liga ou desliga uma região — e desmarca "Nenhuma", que passou a ser contraditória.
     *
     * A exclusividade mora aqui, e não na tela, pela mesma razão de sempre: uma segunda tela que
     * mostrasse estes chips teria de lembrar da regra sozinha.
     */
    fun toggleInjury(area: InjuryArea): TrainingFormState = copy(
        injuries = if (area in injuries) injuries - area else injuries + area,
        noInjuries = false,
    )

    /**
     * Marca ou desmarca "Nenhuma", limpando tudo o que ela contradiz.
     *
     * Limpa inclusive o texto de "Outra": quem acabou de dizer que não tem lesão nenhuma não deve
     * seguir com uma observação sobre a lesão que tinha escrito antes.
     */
    fun toggleNoInjuries(): TrainingFormState = if (noInjuries) {
        copy(noInjuries = false)
    } else {
        copy(noInjuries = true, injuries = emptySet(), otherInjury = false, injuryNotes = "")
    }

    /** Abre ou fecha o campo livre. Fechar apaga o que estava escrito — é o que "desmarquei" quer dizer. */
    fun toggleOtherInjury(): TrainingFormState = if (otherInjury) {
        copy(otherInjury = false, injuryNotes = "")
    } else {
        copy(otherInjury = true, noInjuries = false)
    }
}

/**
 * O formulário conferido.
 *
 * Só peso e altura têm o que conferir: nível, objetivo, dias e regiões lesionadas são escolhas de
 * listas fechadas, e a observação de "Outra" é texto livre que ninguém pode errar. Campo vazio
 * continua válido — todos são opcionais, e o onboarding deixa pular.
 */
internal fun TrainingFormState.validated(): TrainingFormState = copy(
    weightError = TrainingFormValidation.validateWeight(weight),
    heightError = TrainingFormValidation.validateHeight(height),
)
