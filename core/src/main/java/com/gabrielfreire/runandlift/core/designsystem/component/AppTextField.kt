package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.gabrielfreire.runandlift.core.designsystem.Dimens

/**
 * Campo de texto do app.
 *
 * O que ele padroniza: a mensagem de erro fica **abaixo** do campo, ocupa espaço só quando existe,
 * e é anunciada por leitor de tela pelo `semantics { error(...) }` — sem isso, quem usa TalkBack
 * percebe a borda vermelha e nada mais.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val hasError = errorMessage != null

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            singleLine = true,
            isError = hasError,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            visualTransformation = visualTransformation,
            trailingIcon = trailingContent,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { if (errorMessage != null) error(errorMessage) },
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = Dimens.SpaceLarge,
                    top = Dimens.SpaceXSmall,
                ),
            )
        }
    }
}

/**
 * Campo de senha com alternância de visibilidade.
 *
 * O alternador é **texto, não ícone**: "Mostrar" e "Ocultar" são inequívocos para quem não
 * reconhece o desenho de olho riscado, que é justamente o público de D11.
 *
 * @param showLabel rótulo do alternador quando a senha está oculta.
 * @param hideLabel rótulo quando está visível. Vêm de fora porque `:core` não tem recursos de
 *   texto — o design system não decide idioma.
 */
@Composable
fun AppPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    showLabel: String,
    hideLabel: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
) {
    var visible by remember { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            AppTextButton(
                text = if (visible) hideLabel else showLabel,
                onClick = { visible = !visible },
                enabled = enabled,
            )
        },
    )
}
