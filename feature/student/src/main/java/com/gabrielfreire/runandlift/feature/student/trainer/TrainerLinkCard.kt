package com.gabrielfreire.runandlift.feature.student.trainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.text.label

/**
 * Quem treina este aluno, e em que pé está isso.
 *
 * **Encerrar aparece em todos os estados vigentes**, e é a decisão que importa aqui: o aluno desfaz
 * o vínculo sozinho, sem depender de o treinador concordar. Um acompanhamento que só a outra parte
 * consegue terminar não é acompanhamento, é assinatura — e quem revoga o acesso aos próprios dados
 * de saúde não pede permissão a ninguém (LGPD art. 18).
 *
 * Pausar não fica aqui: pausa é combinada, e o aluno que quer parar por um tempo fala com o
 * treinador — a tela dele tem o botão. O que o aluno tem sozinho é sair.
 */
@Composable
internal fun TrainerLinkCard(
    link: Link,
    enabled: Boolean,
    onStatusChange: (LinkStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = link.trainerName.ifBlank { stringResource(R.string.student_trainer_unnamed) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(text = link.status.label(), style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
                // Convite recebido: aceitar é do aluno, e é a contraparte confirmando quem propôs.
                if (link.status == LinkStatus.INVITED) {
                    AppTextButton(
                        text = stringResource(R.string.student_trainer_accept),
                        onClick = { onStatusChange(LinkStatus.ACTIVE) },
                        enabled = enabled,
                    )
                }

                AppTextButton(
                    text = stringResource(
                        if (link.isPending) R.string.student_trainer_cancel else R.string.student_trainer_end,
                    ),
                    onClick = { onStatusChange(LinkStatus.ENDED) },
                    enabled = enabled,
                )
            }
        }
    }
}

@LightDarkPreviews
@Composable
private fun TrainerLinkCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                // Ativo e pendente juntos: são os dois estados com botões diferentes.
                TrainerLinkCard(link = previewLink(LinkStatus.ACTIVE), enabled = true, onStatusChange = {})
                TrainerLinkCard(link = previewLink(LinkStatus.REQUESTED), enabled = true, onStatusChange = {})
            }
        }
    }
}
