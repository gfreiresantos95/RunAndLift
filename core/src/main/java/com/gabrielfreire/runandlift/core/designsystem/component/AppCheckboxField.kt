package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

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
 * O que vem **abaixo** da linha — texto de apoio e erro — começa alinhado com o rótulo, e não com a
 * borda do componente: são continuação da frase da caixa, e uma frase que recomeça alinhada com o
 * quadradinho parece pertencer a outro item da lista.
 *
 * @param supportingText condição ou ressalva do que se está marcando ("Opcional, e você pode
 *   cancelar quando quiser"). Fica fora do alvo de toque: é informação sobre a escolha, não parte
 *   do que o leitor de tela precisa anunciar para decidir.
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
    supportingText: String? = null,
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

            Column {
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

                supportingText?.let { message ->
                    Footnote(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                errorMessage?.let { message ->
                    Footnote(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * Linha abaixo da caixa, recuada até onde o rótulo começa.
 *
 * O recuo é **derivado**, não escolhido: o `Checkbox` do Material ocupa o alvo de toque mínimo, e o
 * rótulo entra depois de um respiro. Somar os dois é o que mantém o alinhamento se o piso de toque
 * do projeto mudar um dia.
 */
@Composable
private fun Footnote(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.padding(start = Dimens.SpaceSmall),
    )
}

/**
 * Os três estados que aparecem no cadastro: marcado, desmarcado e obrigatório em falta. O rótulo
 * longo é proposital — é o formato real de um aceite, e é onde a quebra de linha se confere.
 */
@LightDarkPreviews
@Composable
private fun AppCheckboxFieldPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            ) {
                AppCheckboxField(
                    checked = true,
                    onCheckedChange = {},
                    text = "Li e concordo com os Termos de Uso e a Política de Privacidade.",
                )

                AppCheckboxField(
                    checked = false,
                    onCheckedChange = {},
                    text = "Quero receber dicas de treino e novidades por e-mail.",
                    supportingText = "Opcional, e você pode cancelar quando quiser.",
                )

                AppCheckboxField(
                    checked = false,
                    onCheckedChange = {},
                    text = "Li e concordo com os Termos de Uso e a Política de Privacidade.",
                    errorMessage = "Para criar a conta é preciso aceitar os Termos de Uso.",
                )
            }
        }
    }
}
