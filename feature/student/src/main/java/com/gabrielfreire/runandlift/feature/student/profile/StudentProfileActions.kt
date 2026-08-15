package com.gabrielfreire.runandlift.feature.student.profile

import androidx.compose.runtime.Immutable

/**
 * O que a tela de perfil de treino faz, fora dos campos.
 *
 * Separado de [com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormActions] pela
 * mesma razão que separa `SignUpActions` de `ProfileFormActions` no fluxo de entrada: são coisas de
 * natureza diferente. Um lado descreve o treino, o outro governa a tela — enviar, sair, voltar.
 *
 * @param onSaved a gravação deu certo. Separado de [onBack] porque as duas voltam para o mesmo
 *   lugar e só uma delas leva o recibo junto: sair pela seta não confirma nada, porque nada foi
 *   gravado. Ver `SavedResult`.
 */
@Immutable
internal data class StudentProfileActions(val onSubmit: () -> Unit, val onSaved: () -> Unit, val onBack: () -> Unit)
