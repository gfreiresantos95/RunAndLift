package com.gabrielfreire.runandlift.feature.auth.text
import androidx.annotation.StringRes
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Todo texto que muda conforme o papel, num arquivo só.
 *
 * Antes estavam espalhadas: o rótulo da etiqueta dentro do `when` do próprio chip, o subtítulo do
 * cadastro no fim de `SignUpScreen.kt`, e as três finalidades de campo no fim de `SignUpForm.kt`.
 * Eram cinco decisões da mesma natureza — "o que o app diz para o aluno e o que diz para o
 * treinador" — em três arquivos, e responder "o cadastro fala diferente com cada perfil em quantos
 * lugares?" exigia abrir os três.
 *
 * Elas **não** ficam junto de [ActiveRole] porque ele mora em `:data`, que não tem recursos de
 * string. Este arquivo é o lugar mais perto do enum que ainda pode conhecer `R.string` — a mesma
 * razão que mantém `AuthFailure.message()` fora de `:data`.
 *
 * O tipo é `ActiveRole?` e não `ActiveRole`: o cadastro é alcançável sem perfil conhecido, e nesse
 * caso vale o texto do aluno, que é o público maior. `subtitle` é a exceção — sem perfil ele usa
 * uma promessa genérica, porque prometer o que o treinador recebe a quem talvez seja aluno é pior
 * do que não prometer nada.
 */
@StringRes
internal fun ActiveRole.chipLabel(): Int = when (this) {
    ActiveRole.STUDENT -> R.string.auth_role_student
    ActiveRole.TRAINER -> R.string.auth_role_trainer
}

/** O que a conta vai fazer, na voz de quem vai usá-la — é a promessa que justifica pedir os dados. */
@StringRes
internal fun ActiveRole?.signUpSubtitle(): Int = when (this) {
    ActiveRole.STUDENT -> R.string.auth_sign_up_subtitle_student
    ActiveRole.TRAINER -> R.string.auth_sign_up_subtitle_trainer
    null -> R.string.auth_sign_up_subtitle
}

/** Para que serve o nome: encontrar o aluno numa lista, ou ser visto pelos alunos. */
@StringRes
internal fun ActiveRole?.nameSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_name_support_trainer
    else -> R.string.auth_name_support_student
}

/**
 * Para que serve a data: ajustar esforço, no aluno; confirmar idade para criar conta, no treinador.
 * A finalidade é dita no próprio campo — o que a LGPD chama de informação adequada (art. 9º).
 */
@StringRes
internal fun ActiveRole?.birthDateSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_birth_date_support_trainer
    else -> R.string.auth_birth_date_support_student
}

/** O texto do aluno diz "Opcional" e o do treinador não — porque para ele o campo é exigido. */
@StringRes
internal fun ActiveRole?.phoneSupport(): Int = when (this) {
    ActiveRole.TRAINER -> R.string.auth_phone_support_trainer
    else -> R.string.auth_phone_support_student
}
