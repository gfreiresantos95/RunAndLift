package com.gabrielfreire.runandlift.feature.student.profile

/**
 * O que ainda falta no perfil de treino do aluno.
 *
 * Um campo por pergunta, e não uma contagem, porque quem lê isto precisa saber **o quê** falta: o
 * aviso da home diz quantas coisas faltam, e a tela de edição abre já apontando para elas.
 *
 * Peso, altura e restrições só contam como faltando **se houver consentimento de dado de saúde**.
 * Sem ele, o app não pediu esses dados e não os pode guardar — cobrá-los no aviso seria cobrar uma
 * resposta que a pessoa já recusou dar, o que é o oposto de consentimento.
 */
internal data class MissingStudentData(
    val level: Boolean = false,
    val goal: Boolean = false,
    val availableDays: Boolean = false,
    val measures: Boolean = false,
    val restrictions: Boolean = false,
    /** O aceite do aviso de dado de saúde, que ainda não foi dado nem recusado nesta versão. */
    val healthConsent: Boolean = false,
) {

    /** Quantas perguntas seguem sem resposta — é o número que o aviso da home mostra. */
    val count: Int
        get() = listOf(level, goal, availableDays, measures, restrictions, healthConsent).count { it }

    /** Se há algo a completar. */
    val any: Boolean get() = count > 0
}
