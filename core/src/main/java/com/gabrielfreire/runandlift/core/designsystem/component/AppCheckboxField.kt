package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.Dimens

/**
 * Caixa de seleção com rótulo em frase — aceite de termos, opt-in de comunicação e afins.
 *
 * Três coisas que ela resolve e que a tela não deve refazer:
 * - **A linha inteira é o alvo de toque**, não o quadradinho de 20 dp. `toggleable` no `Row` com
 *   `Role.Checkbox` é o que faz o rótulo alternar junto e o leitor de tela anunciar um controle só,
 *   em vez de um botão sem nome seguido de um texto solto.
 * - **Erro é anunciado**, pelo mesmo `semantics { error(...) }` do [AppTextField]. Aceite pendente
 *   sem isso é uma borda que quem usa TalkBack não percebe.
 * - **Altura mínima de 48 dp**, o piso de alvo de toque do projeto (E0-09).
 *
 * @param errorMessage motivo de o aceite ser obrigatório e estar faltando. Aparece abaixo, em texto
 *   — a cor é reforço, nunca o único canal.
 */
@Composable
fun AppCheckboxField(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorMessage: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                )
                .defaultMinSize(minHeight = Dimens.MinTouchTarget)
                .semantics { if (errorMessage != null) error(errorMessage) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // `onCheckedChange = null` entrega o toque ao Row: com os dois clicáveis, o leitor de
            // tela anunciaria dois controles para uma decisão só.
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = Dimens.SpaceSmall,
                    top = Dimens.SpaceSmall,
                    bottom = Dimens.SpaceSmall,
                ),
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Dimens.SpaceLarge),
            )
        }
    }
}
