package com.gabrielfreire.runandlift.feature.student.home

import java.time.DayOfWeek

/**
 * O painel da home do aluno: o próximo treino, a semana e o último recorde.
 *
 * **Hoje os números são de exemplo, e a tela diz isso.** Não há treino registrado no banco porque
 * não há treino no produto ainda — a aba de treinos é uma tela vazia. A alternativa a este exemplo
 * seria uma home com um card de saudação e nada, que não deixa ninguém julgar se o painel mostra o
 * que importa. Quando `WorkoutRepository` existir, o que muda é quem constrói este objeto; a tela e
 * as contas abaixo continuam as mesmas, que é a razão de os dados estarem aqui e não espalhados nos
 * composables.
 *
 * **O que o painel mostra veio de olhar o mercado, e a escolha tem um lado.** Os aplicativos de
 * treino que seguram gente por mais de um ano são os que abrem em constância — sequência de
 * semanas, treinos cumpridos, o próximo passo — e não em gráfico de peso corporal. O peso é o
 * número que uma semana boa às vezes piora, e abrir o app nele ensina a pessoa a se julgar pelo
 * que ela não controla. Aqui o peso continua no perfil, onde o treinador o lê; a home mostra o que
 * a pessoa fez.
 *
 * As contas ficam neste arquivo, e não nos composables, porque um teste de JVM alcança este objeto
 * e não alcança uma tela.
 *
 * @param sessionsDone treinos registrados nesta semana.
 * @param sessionsPlanned treinos previstos para a semana, que vem dos dias escolhidos no perfil.
 * @param streakWeeks semanas seguidas com pelo menos um treino. É o número que o mercado inteiro
 *   descobriu ser o mais motivador, e o único do painel que mede persistência em vez de esforço.
 * @param volumeKg carga total levantada na semana — série a série, peso vezes repetições.
 * @param activeMinutes tempo em treino na semana.
 * @param doneDays dias em que houve treino registrado.
 * @param plannedDays dias em que há treino previsto. Um dia pode estar nos dois conjuntos: é o dia
 *   previsto que foi cumprido, e é o caso comum de quem está em dia.
 */
internal data class StudentDashboard(
    val nextWorkoutName: String,
    val nextWorkoutFocus: String,
    val nextWorkoutExercises: Int,
    val nextWorkoutMinutes: Int,
    val sessionsDone: Int,
    val sessionsPlanned: Int,
    val streakWeeks: Int,
    val volumeKg: Int,
    val activeMinutes: Int,
    val doneDays: Set<DayOfWeek>,
    val plannedDays: Set<DayOfWeek>,
    val recordExercise: String,
    val recordLoad: String,
) {

    /**
     * Quantos treinos ainda faltam para fechar a semana, nunca abaixo de zero.
     *
     * Quem treinou mais do que combinou não vê "-1 treino": passar do previsto é boa notícia, e o
     * painel não tem por que transformá-la numa dívida negativa que ninguém entende.
     */
    val remainingSessions: Int
        get() = (sessionsPlanned - sessionsDone).coerceAtLeast(minimumValue = 0)

    /**
     * A semana fechada, ou não.
     *
     * Serve para a tela trocar a frase de "faltam 2" para "semana completa" — e é a única
     * comemoração do painel, porque é a única coisa que a pessoa de fato terminou.
     */
    val weekComplete: Boolean
        get() = sessionsPlanned > 0 && sessionsDone >= sessionsPlanned

    /**
     * Os sete dias da semana, de segunda a domingo, com o estado de cada um.
     *
     * A ordem é fixa e começa na segunda porque é assim que se lê uma semana de treino no Brasil —
     * `DayOfWeek.entries` já vem nessa ordem, e depender disso é mais honesto do que reordenar uma
     * lista que já está certa.
     *
     * **Registrado ganha de previsto** quando o dia está nos dois conjuntos: o que aconteceu vale
     * mais do que o que estava marcado para acontecer.
     */
    val week: List<Pair<DayOfWeek, TrainingDayState>>
        get() = DayOfWeek.entries.map { day -> day to stateOf(day) }

    private fun stateOf(day: DayOfWeek): TrainingDayState = when {
        day in doneDays -> TrainingDayState.DONE
        day in plannedDays -> TrainingDayState.PLANNED
        else -> TrainingDayState.REST
    }

    companion object {

        /**
         * O exemplo que a home mostra hoje.
         *
         * Os números são de uma semana **quase** cumprida, e não de uma semana perfeita: três de
         * quatro treinos, com um dia previsto ainda em aberto. É o estado em que o painel tem algo
         * a dizer — uma semana 4/4 esconde a frase "falta 1 treino", que é a única linha do painel
         * capaz de fazer alguém sair de casa.
         */
        val SAMPLE = StudentDashboard(
            nextWorkoutName = "Treino B",
            nextWorkoutFocus = "Puxar · costas e bíceps",
            nextWorkoutExercises = 6,
            nextWorkoutMinutes = 55,
            sessionsDone = 3,
            sessionsPlanned = 4,
            streakWeeks = 5,
            volumeKg = 12_480,
            activeMinutes = 197,
            doneDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            plannedDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
            ),
            recordExercise = "Agachamento livre",
            recordLoad = "92,5 kg",
        )
    }
}
