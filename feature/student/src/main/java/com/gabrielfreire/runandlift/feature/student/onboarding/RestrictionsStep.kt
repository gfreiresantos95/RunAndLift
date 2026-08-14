package com.gabrielfreire.runandlift.feature.student.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextField
import com.gabrielfreire.runandlift.feature.student.R

/**
 * Passo de lesões e restrições.
 *
 * **Texto livre**, e não uma lista de caixas: a lista nunca contém o caso da pessoa, e o que ela
 * escrever aqui é lido por um profissional, não por um algoritmo. "Dói o ombro direito quando levanto
 * acima da cabeça" não cabe em caixa nenhuma e é exatamente o que o treinador precisa saber.
 *
 * Sem validação, pelo mesmo motivo: não há formato errado para isto.
 */
@Composable
internal fun RestrictionsStep(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    AppTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(R.string.student_field_restrictions),
        supportingText = stringResource(R.string.student_field_restrictions_support),
        modifier = modifier.fillMaxWidth(),
    )
}

@LightDarkPreviews
@Composable
private fun RestrictionsStepPreview() {
    RunAndLiftTheme {
        RestrictionsStep(
            value = stringResource(R.string.student_field_restrictions_sample),
            onChange = {},
        )
    }
}
