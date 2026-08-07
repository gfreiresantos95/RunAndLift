package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.Dimens

/**
 * Botão de ação principal.
 *
 * Três coisas que ele resolve e que não devem ser refeitas na tela:
 * - **Altura mínima de 48 dp**, o piso de alvo de toque do projeto. O botão do Material tem 40 dp
 *   por padrão, abaixo do que o público exige (backlog E0-09, D11).
 * - **Estado de carregamento embutido**: enquanto carrega, o botão desabilita e troca o rótulo por
 *   um indicador, sem mudar de tamanho. Tela que faz isso à mão erra o tamanho e pisca.
 * - **Largura cheia por padrão**, que é o formato usado nos formulários do app.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Dimens.ComfortableTouchTarget),
        enabled = enabled && !loading,
    ) {
        ButtonContent(text = text, loading = loading)
    }
}

/** Ação secundária, com o mesmo alvo de toque e o mesmo comportamento de carregamento. */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Dimens.ComfortableTouchTarget),
        enabled = enabled && !loading,
    ) {
        ButtonContent(text = text, loading = loading)
    }
}

/** Ação terciária, para "esqueci minha senha" e afins. Continua respeitando os 48 dp. */
@Composable
fun AppTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = Dimens.MinTouchTarget),
        enabled = enabled,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean) {
    // O Box mantém a caixa do mesmo tamanho nos dois estados: sem ele, o botão encolhe ao entrar
    // em carregamento e o layout salta.
    Box(contentAlignment = Alignment.Center) {
        // alpha(0f) e não remoção do texto: o rótulo continua ocupando espaço, então a largura do
        // botão não muda ao entrar em carregamento.
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.alpha(if (loading) 0f else 1f),
        )
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(PROGRESS_SIZE),
                strokeWidth = PROGRESS_STROKE,
                color = LocalContentColor.current,
            )
        }
    }
}

private val PROGRESS_SIZE = 20.dp
private val PROGRESS_STROKE = 2.dp
