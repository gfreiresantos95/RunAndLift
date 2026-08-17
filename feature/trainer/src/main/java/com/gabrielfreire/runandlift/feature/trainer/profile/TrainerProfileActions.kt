package com.gabrielfreire.runandlift.feature.trainer.profile

import androidx.compose.runtime.Immutable

/**
 * O que a tela de perfil profissional faz, fora dos campos.
 *
 * Separado de [com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormActions]
 * porque são coisas de natureza diferente: um lado descreve a atuação, o outro governa a tela —
 * enviar, sair, voltar.
 *
 * @param onSaved a gravação deu certo. Separado de [onBack] porque as duas voltam para o mesmo
 *   lugar e só uma delas leva o recibo junto: sair pela seta não confirma nada, porque nada foi
 *   gravado. Ver `SavedResult`.
 */
@Immutable
internal data class TrainerProfileActions(val onSubmit: () -> Unit, val onSaved: () -> Unit, val onBack: () -> Unit)
