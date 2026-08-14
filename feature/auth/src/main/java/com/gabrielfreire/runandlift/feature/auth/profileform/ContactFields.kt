package com.gabrielfreire.runandlift.feature.auth.profileform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppMaskedTextField
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R
import com.gabrielfreire.runandlift.feature.auth.text.birthDateSupport
import com.gabrielfreire.runandlift.feature.auth.text.phoneSupport
import com.gabrielfreire.runandlift.feature.auth.validation.AuthFormValidation
import com.gabrielfreire.runandlift.feature.auth.validation.message

/**
 * Nascimento e contato — os dois campos cuja finalidade muda conforme o perfil, e o celular, que
 * muda também de obrigatoriedade: opcional para o aluno, exigido do treinador.
 *
 * É o único bloco que a conclusão de cadastro reaproveita inteiro: lá a conta já existe, mas o que
 * o Google não informou é exatamente isto.
 */
@Composable
internal fun ContactFields(
    form: ProfileFormState,
    formActions: ProfileFormActions,
    role: ActiveRole?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AppMaskedTextField(
            value = form.birthDate,
            onValueChange = formActions.onBirthDateChange,
            label = stringResource(id = R.string.auth_birth_date),
            mask = AuthFormValidation.BIRTH_DATE_MASK,
            errorMessage = form.birthDateError?.message(),
            supportingText = stringResource(id = role.birthDateSupport()),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppMaskedTextField(
            value = form.phone,
            onValueChange = formActions.onPhoneChange,
            label = stringResource(id = R.string.auth_phone),
            mask = AuthFormValidation.PHONE_MASK,
            errorMessage = form.phoneError?.message(),
            supportingText = stringResource(id = role.phoneSupport()),
            enabled = enabled,
            // `Done` para os dois perfis: o celular é o último campo **digitado** deste bloco, e o
            // que vem depois dele são estado e cidade, que se escolhem numa lista. Um "próximo"
            // aqui moveria o foco para um campo que não aceita teclado, deixando-o aberto e inútil
            // por cima da tela que a pessoa precisa tocar.
            imeAction = ImeAction.Done,
        )
    }
}

/**
 * Os dois perfis lado a lado: o texto de apoio do celular do aluno começa com "Opcional" e o do
 * treinador não. É a diferença que este bloco existe para carregar, e a única forma de conferi-la
 * é vendo as duas versões juntas.
 */
@LightDarkPreviews
@Composable
private fun ContactFieldsPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXLarge),
            ) {
                ContactFields(
                    form = previewStudentForm(),
                    formActions = previewProfileFormActions(),
                    role = ActiveRole.STUDENT,
                    enabled = true,
                )

                ContactFields(
                    form = previewTrainerForm(),
                    formActions = previewProfileFormActions(),
                    role = ActiveRole.TRAINER,
                    enabled = true,
                )
            }
        }
    }
}
