package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Campo cujo valor não se digita: toca-se, abre uma tela de escolha, e ele volta preenchido.
 *
 * Parece um [AppTextField] de propósito. Num formulário, um controle que não se pareça com os
 * vizinhos vira a pergunta "isso também é para preencher?" — a única diferença visível é o triângulo
 * à direita, que é a convenção para "aqui abre uma escolha".
 *
 * Três decisões de mecânica que não são óbvias:
 * - **Somente-leitura, e não desabilitado.** `enabled = false` pintaria o campo de cinza, e cinza
 *   quer dizer "indisponível" — mas este campo está perfeitamente disponível, só não pelo teclado.
 * - **A área de toque é uma camada por cima, e não um `clickable` no campo.** Um `OutlinedTextField`
 *   consome o toque para posicionar o cursor mesmo em somente-leitura, então um clique no campo
 *   nunca chegaria a quem está por baixo. A camada resolve isso e ainda garante que a linha de apoio
 *   também abra a tela — quem lê "Escolha o seu estado" toca ali.
 * - **Vazio fica vazio.** O convite vai em [supportingText], e não desenhado dentro da caixa em
 *   cinza-claro: um texto dentro do campo, num campo que já não aceita digitação, é indistinguível
 *   de um valor já escolhido.
 *
 * O [semantics] com `mergeDescendants` junta rótulo, valor e ação num nó só: sem ele, o TalkBack
 * anuncia um campo de texto e, em seguida, um botão sem nome.
 *
 * @param value o que já foi escolhido, já formatado para leitura — `São Paulo - SP`. A formatação é
 *   de quem chama: este componente não sabe o que está sendo escolhido.
 * @param supportingText o convite enquanto não há escolha ("Escolha o seu estado"), ou o que falta
 *   para a escolha fazer sentido.
 * @param enabled `false` quando a escolha ainda não é possível — a cidade antes do estado. Aí sim o
 *   cinza está certo: é indisponível mesmo, e [supportingText] diz por quê.
 */
@Composable
fun AppSelectField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { role = Role.Button },
    ) {
        AppTextField(
            value = value,
            onValueChange = {},
            label = label,
            errorMessage = errorMessage,
            supportingText = supportingText,
            enabled = enabled,
            readOnly = true,
            trailingContent = {
                Icon(painter = painterResource(AppIcons.Dropdown), contentDescription = null)
            },
        )

        // Cobre o campo inteiro, incluindo a linha de apoio. Sem indicação visual própria: o campo
        // por baixo já é o desenho, e um ripple retangular por cima dele ficaria maior que a caixa.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled, onClick = onClick),
        )
    }
}

/**
 * Os quatro estados: preenchido, vazio com o convite, desabilitado à espera do estado, e com erro.
 * O terceiro é o que se confere com mais atenção — é ele que precisa parecer "ainda não" e não
 * "quebrado".
 */
@LightDarkPreviews
@Composable
private fun AppSelectFieldPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                AppSelectField(
                    value = PreviewSamples.Picker.STATE_VALUE,
                    label = PreviewSamples.Picker.STATE_LABEL,
                    onClick = {},
                )

                AppSelectField(
                    value = "",
                    label = PreviewSamples.Picker.CITY_LABEL,
                    onClick = {},
                    supportingText = PreviewSamples.Picker.CITY_SUPPORT,
                )

                AppSelectField(
                    value = "",
                    label = PreviewSamples.Picker.CITY_LABEL,
                    onClick = {},
                    supportingText = PreviewSamples.Picker.CITY_BLOCKED,
                    enabled = false,
                )

                AppSelectField(
                    value = "",
                    label = PreviewSamples.Picker.STATE_LABEL,
                    onClick = {},
                    supportingText = PreviewSamples.Picker.STATE_SUPPORT,
                    errorMessage = PreviewSamples.Picker.STATE_REQUIRED,
                )
            }
        }
    }
}
