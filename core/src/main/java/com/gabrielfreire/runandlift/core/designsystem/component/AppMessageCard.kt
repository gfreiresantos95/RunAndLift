package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.ColorRole
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors

/**
 * A mensagem que aparece quando algo deu errado e continua na tela até deixar de ser verdade.
 *
 * Substitui o `Text` vermelho solto que cada tela desenhava à mão, e conserta três coisas de uma
 * vez:
 *
 * - **Cor não era o único canal, era o único mesmo.** Um parágrafo vermelho no meio de um formulário
 *   depende inteiramente da cor para ser lido como erro. Aqui vem o ícone junto, que é a regra do
 *   projeto para qualquer uso de cor com significado (E0-09).
 * - **Ninguém anunciava.** O texto surgia depois de um toque no botão, longe do foco, e o TalkBack
 *   não dizia nada: a pessoa tocava em "Salvar", nada acontecia e não havia como saber por quê. O
 *   `liveRegion` faz a frase ser falada assim que entra.
 * - **Não tinha forma.** Solto entre os campos, o texto competia com as linhas de apoio deles. Como
 *   bloco com fundo próprio, ele se separa do formulário — que é o que um erro de envio é.
 *
 * **Fica na tela, e não é um snackbar.** Snackbar some sozinho, e o que some não serve para uma
 * falha que a pessoa precisa resolver: ela some justamente enquanto se lê o formulário procurando o
 * que corrigir. Snackbar é para confirmação — ver [AppSnackbarHost].
 *
 * @param role o papel de cor. `critical` para o que impediu a ação; `attention` para o que a pessoa
 *   precisa saber mas não impede nada. Vem de fora porque a diferença é de significado, e quem a
 *   conhece é a tela.
 */
@Composable
fun AppMessageCard(text: String, modifier: Modifier = Modifier, role: ColorRole? = null) {
    val resolved = role ?: MaterialTheme.extendedColors.critical

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        color = resolved.container,
        contentColor = resolved.onContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
        ) {
            // Nulo: a frase ao lado é a mensagem inteira, e o ícone só a reforça para o olho.
            Icon(painter = painterResource(AppIcons.Alert), contentDescription = null)

            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Os dois papéis, um sob o outro. Vale abrir nos dois temas: é aqui que se confere que o vermelho
 * de falha e o âmbar de pendência continuam distinguíveis um do outro no escuro.
 */
@LightDarkPreviews
@Composable
private fun AppMessageCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMedium),
            ) {
                AppMessageCard(text = PreviewSamples.State.SAVE_FAILED)
                AppMessageCard(
                    text = PreviewSamples.State.OFFLINE,
                    role = MaterialTheme.extendedColors.attention,
                )
            }
        }
    }
}
