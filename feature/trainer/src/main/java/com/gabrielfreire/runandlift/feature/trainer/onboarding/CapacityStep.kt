package com.gabrielfreire.runandlift.feature.trainer.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.professionalform.TrainerFormState
import com.gabrielfreire.runandlift.feature.trainer.validation.message

/**
 * Passo da capacidade de atendimento.
 *
 * O texto de apoio diz **para que serve**, e não o formato: o número existe para o treinador sair
 * da vitrine quando encher, e não para o app policiar quantos alunos ele aceita. Sem essa frase, a
 * pergunta parece uma cobrança de meta.
 *
 * Teclado numérico, e só dígito entra: "vinte" não é um número que o app saiba comparar com a
 * carteira de alunos.
 */
@Composable
internal fun CapacityStep(
    form: TrainerFormState,
    onMaxStudentsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = form.maxStudents,
        onValueChange = onMaxStudentsChange,
        label = stringResource(R.string.trainer_field_capacity),
        modifier = modifier,
        errorMessage = form.maxStudentsError?.message(),
        supportingText = stringResource(R.string.trainer_field_capacity_support),
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
    )
}

@LightDarkPreviews
@Composable
private fun CapacityStepPreview() {
    RunAndLiftTheme {
        CapacityStep(form = TrainerFormState(maxStudents = "20"), onMaxStudentsChange = {})
    }
}
