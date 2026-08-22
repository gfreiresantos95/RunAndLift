package com.gabrielfreire.runandlift.feature.student.workouts

/**
 * O que se pode fazer na aba de treinos — que é pouco, porque ela é de leitura.
 *
 * Abrir um dia e tentar de novo. Não há prescrever, editar nem marcar como feito: prescrever é ato
 * do treinador, e o registro do que foi executado é outra coleção e outra tela (E6-02).
 */
internal data class StudentWorkoutsActions(val onOpenDay: (Int) -> Unit, val onRetry: () -> Unit)
