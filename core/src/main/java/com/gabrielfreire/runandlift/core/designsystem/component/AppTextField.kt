package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.gabrielfreire.runandlift.core.R
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import kotlinx.coroutines.launch

/**
 * Campo de texto do app.
 *
 * O que ele padroniza: a linha abaixo do campo é **uma só** e ocupa espaço só quando tem o que
 * dizer — erro quando há erro, [supportingText] quando não há. Trocar uma pela outra no mesmo
 * lugar evita o campo mudar de altura quando a validação falha, que é quando o layout saltar mais
 * atrapalha.
 *
 * O erro é anunciado por leitor de tela pelo `semantics { error(...) }` — sem isso, quem usa
 * TalkBack percebe a borda vermelha e nada mais.
 *
 * Ao receber foco, o campo **inteiro** é trazido para dentro da área visível — rótulo, caixa e a
 * linha de apoio abaixo dela. O `BasicTextField` por dentro já faz isso sozinho, mas só com o
 * retângulo do cursor: o suficiente para enxergar o que se digita, e não para ler a regra ou o erro
 * que estão logo abaixo, que é justamente onde a linha de apoio some atrás do teclado.
 *
 * @param supportingText regra do campo, dita **antes** de o usuário errar. "Mínimo de 8
 *   caracteres" na entrada vale mais que "senha muito curta" depois do envio.
 * @param onImeAction o que a tecla de ação do teclado dispara. Sem isso ela não faz nada, e quem
 *   está de teclado aberto precisa fechá-lo para alcançar o botão.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    onImeAction: (() -> Unit)? = null,
) {
    val hasError = errorMessage != null
    val bringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoView),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            singleLine = true,
            isError = hasError,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onGo = { onImeAction?.invoke() },
                onNext = { onImeAction?.invoke() },
            ),
            visualTransformation = visualTransformation,
            trailingIcon = trailingContent,
            modifier = Modifier
                .fillMaxWidth()
                // Pedir de novo a cada evento de foco é barato: se o campo já está visível, o
                // pedido não move nada. O que ele resolve é o campo que ganha foco pela tecla
                // "próximo" do teclado, sem toque nenhum para rolar a tela antes.
                .onFocusEvent { if (it.isFocused) scope.launch { bringIntoView.bringIntoView() } }
                .semantics { if (errorMessage != null) error(errorMessage) },
        )

        FieldFootnote(errorMessage = errorMessage, supportingText = supportingText)
    }
}

/** Erro tem precedência sobre a regra: quando os dois existem, quem falhou é a informação nova. */
@Composable
private fun FieldFootnote(errorMessage: String?, supportingText: String?) {
    val text = errorMessage ?: supportingText ?: return

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (errorMessage != null) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.padding(start = Dimens.SpaceLarge, top = Dimens.SpaceXSmall),
    )
}

/**
 * Campo de senha com alternância de visibilidade.
 *
 * O alternador é o **ícone de olho**, e não o texto "Mostrar" que havia antes (ADR-0011 reverte o
 * ADR-0009): o desenho é a convenção que todo app de senha usa, e o texto competia visualmente com
 * o conteúdo do próprio campo. O que o texto garantia — que ninguém ficasse sem entender o
 * controle — passou para a descrição de acessibilidade, que o leitor de tela anuncia.
 *
 * @param showLabel descrição do alternador quando a senha está oculta ("Mostrar senha").
 * @param hideLabel descrição quando está visível. Vêm de fora porque `:core` não tem recursos de
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
    supportingText: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
) {
    var visible by remember { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        errorMessage = errorMessage,
        supportingText = supportingText,
        enabled = enabled,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onImeAction = onImeAction,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            IconButton(onClick = { visible = !visible }, enabled = enabled) {
                Icon(
                    painter = painterResource(
                        id = if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                    ),
                    contentDescription = if (visible) hideLabel else showLabel,
                )
            }
        },
    )
}
