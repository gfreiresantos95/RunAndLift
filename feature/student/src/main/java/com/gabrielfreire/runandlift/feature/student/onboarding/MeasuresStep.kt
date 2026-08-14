package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.trainingform.TrainingFormState
import com.gabrielfreire.runandlift.feature.student.validation.message

/**
 * Passo de peso e altura — o primeiro que só existe com consentimento.
 *
 * O texto de apoio de cada campo diz **para que serve**, e não o formato: quem informa peso quer
 * saber por que o app pergunta, e "em quilos" já está no rótulo.
 *
 * Teclado numérico nos dois, com o de peso aceitando decimal — 72,5 é um peso comum, e obrigar o
 * arredondamento produziria um dado pior do que o que a pessoa tem na balança.
 */
@Composable
internal fun MeasuresStep(
    form: TrainingFormState,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        AppTextField(
            value = form.weight,
            onValueChange = onWeightChange,
            label = stringResource(R.string.student_field_weight),
            supportingText = stringResource(R.string.student_field_weight_support),
            errorMessage = form.weightError?.message(),
            keyboardType = KeyboardType.Decimal,
        )

        AppTextField(
            value = form.height,
            onValueChange = onHeightChange,
            label = stringResource(R.string.student_field_height),
            supportingText = stringResource(R.string.student_field_height_support),
            errorMessage = form.heightError?.message(),
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        )
    }
}

@LightDarkPreviews
@Composable
private fun MeasuresStepPreview() {
    RunAndLiftTheme {
        MeasuresStep(
            form = TrainingFormState(weight = "72,5", height = "175"),
            onWeightChange = {},
            onHeightChange = {},
        )
    }
}
