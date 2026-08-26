package com.gabrielfreire.runandlift.feature.trainer.studentworkout

/**
 * O que a tela do treino de um aluno faz.
 *
 * Duas ações, e nenhuma delas escreve nada: esta tela é de conferência. Trocar o treino é ato do
 * editor de programa, e daqui só se **vai** até ele.
 *
 * @param onOpenPrograms leva para a aba de treinos, que é onde se escolhe o programa e se atribui.
 *   Sai daqui como retorno de chamada, e não como uma rota, porque quem monta a pilha é a navegação
 *   do módulo — a tela não decide o que acontece com o que está atrás dela.
 * @param onRetry relê a prescrição. Existe porque a falha de leitura tem de oferecer saída.
 */
internal data class StudentWorkoutActions(val onOpenPrograms: () -> Unit, val onRetry: () -> Unit)
