package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Bloco de aviso: um parágrafo que explica alguma coisa, sem ação e sem ícone.
 *
 * Existe porque o cadastro tinha **duas cópias do mesmo desenho** — o aviso de dado de saúde do
 * aluno e o aviso sobre o registro profissional do treinador, cada um com o seu `Surface`, o seu
 * `bodySmall` e o seu recuo escritos à mão. Sendo a mesma vaga do formulário e a mesma intenção
 * (responder a pergunta antes de ela ser feita), o que muda entre os dois é só o texto.
 *
 * **Não é um alerta.** A cor é `surfaceVariant`, não `errorContainer`: quem lê isto não errou nada,
 * e pintar de vermelho um parágrafo informativo ensina a ignorar o vermelho que importa. Para falha
 * de verdade existe o banner de erro da tela.
 *
 * O texto é `bodySmall` porque é leitura de apoio, e não a instrução principal da tela — mas
 * continua em `sp`, então acompanha a fonte do sistema de quem aumentou o corpo do texto (E0-09).
 */
@Composable
fun AppNoticeCard(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
        )
    }
}

/**
 * Um parágrafo longo, que é o formato real: o aviso curto cabe em qualquer lugar, o de três linhas
 * é que revela se o recuo interno aguenta a fonte do sistema aumentada.
 */
@LightDarkPreviews
@Composable
private fun AppNoticeCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                AppNoticeCard(text = PreviewSamples.NOTICE)
            }
        }
    }
}
