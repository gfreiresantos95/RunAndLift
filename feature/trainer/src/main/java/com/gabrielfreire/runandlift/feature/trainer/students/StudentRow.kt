package com.gabrielfreire.runandlift.feature.trainer.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus
import com.gabrielfreire.runandlift.feature.trainer.R
import com.gabrielfreire.runandlift.feature.trainer.text.label

/**
 * Uma pessoa da carteira: nome, em que pé está o vínculo, e o que dá para fazer com ele.
 *
 * **O estado é texto, e não cor.** Ativo, pausado e encerrado se distinguem pela palavra escrita:
 * uma bolinha colorida exigiria legenda e falharia inteira para quem não distingue as cores — a
 * mesma regra do semáforo de aderência, aplicada antes de ele existir.
 *
 * **As ações mudam com o estado, e é isso que faz a linha se explicar sozinha.** Quem pediu para
 * treinar traz "Aceitar" e "Recusar"; quem já treina traz "Pausar" e "Encerrar"; quem saiu não traz
 * nada, porque de encerrado só se volta pelo pedido de quem quer voltar. Nenhum botão fica na tela
 * desabilitado esperando explicação.
 *
 * @param onStatusChange recebe o estado de destino. A linha sabe **quais** transições oferecer; se
 *   elas são permitidas quem diz é o servidor, e é por isso que ela não decide nada além do desenho.
 */
@Composable
internal fun StudentRow(
    link: Link,
    updating: Boolean,
    onStatusChange: (LinkStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ListItemHeight),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                // Nome vazio acontece: o cadastro por Google pode não ter trazido nenhum, e a
                // carteira não pode virar uma lista de linhas em branco por causa disso.
                text = link.studentName.ifBlank { stringResource(R.string.trainer_students_unnamed) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(text = link.status.label(), style = MaterialTheme.typography.bodySmall)

            StudentRowActions(status = link.status, enabled = !updating, onStatusChange = onStatusChange)
        }
    }
}

/**
 * Os botões que este estado oferece.
 *
 * Separado da linha porque é a única parte dela que tem regra — e porque um `when` sobre cinco
 * estados dentro do layout esconderia o layout.
 */
@Composable
private fun StudentRowActions(status: LinkStatus, enabled: Boolean, onStatusChange: (LinkStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
        when (status) {
            LinkStatus.REQUESTED -> {
                RowAction(R.string.trainer_students_accept, enabled) { onStatusChange(LinkStatus.ACTIVE) }
                RowAction(R.string.trainer_students_decline, enabled) { onStatusChange(LinkStatus.ENDED) }
            }

            // Convite feito pelo treinador: quem responde é o aluno, e a única ação daqui é desistir.
            LinkStatus.INVITED ->
                RowAction(R.string.trainer_students_cancel, enabled) { onStatusChange(LinkStatus.ENDED) }

            LinkStatus.ACTIVE -> {
                RowAction(R.string.trainer_students_pause, enabled) { onStatusChange(LinkStatus.PAUSED) }
                RowAction(R.string.trainer_students_end, enabled) { onStatusChange(LinkStatus.ENDED) }
            }

            LinkStatus.PAUSED -> {
                RowAction(R.string.trainer_students_resume, enabled) { onStatusChange(LinkStatus.ACTIVE) }
                RowAction(R.string.trainer_students_end, enabled) { onStatusChange(LinkStatus.ENDED) }
            }

            LinkStatus.ENDED -> Unit
        }
    }
}

@Composable
private fun RowAction(textId: Int, enabled: Boolean, onClick: () -> Unit) {
    AppTextButton(text = stringResource(textId), onClick = onClick, enabled = enabled)
}

@LightDarkPreviews
@Composable
private fun StudentRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // Os dois estados com ações diferentes, um sob o outro: é assim que a lista aparece
                // para quem tem um pedido novo e alunos antigos.
                StudentRow(link = previewLink(LinkStatus.REQUESTED), updating = false, onStatusChange = {})
                StudentRow(link = previewLink(LinkStatus.ACTIVE), updating = false, onStatusChange = {})
            }
        }
    }
}
