package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens

/**
 * Cartão de escolha de papel, usado pelas boas-vindas e pela escolha depois de autenticar.
 *
 * Duas decisões de acessibilidade que não devem ser desfeitas:
 * - **`selectable` com [Role.RadioButton]**, e não `clickable`: é o que faz o leitor de tela
 *   anunciar o cartão como opção escolhível e informar qual está marcada.
 * - **A seleção tem três canais**: o botão de rádio, o contorno e a cor de fundo. Cor sozinha não
 *   comunica estado (E0-09), e num cartão grande a diferença de tom passa despercebida.
 */
@Composable
internal fun RoleOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        colors = if (selected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
        border = if (selected) {
            BorderStroke(width = SELECTED_BORDER, color = MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // `onClick = null`: quem trata o toque é o cartão inteiro. Um rádio clicável próprio
            // criaria um segundo alvo dentro do primeiro e duplicaria o anúncio no leitor de tela.
            RadioButton(selected = selected, onClick = null, enabled = enabled)

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private val SELECTED_BORDER = Dimens.BorderThin * 2
