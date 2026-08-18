package com.gabrielfreire.runandlift.feature.student.trainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppTextButton
import com.gabrielfreire.runandlift.data.model.InviteCode
import com.gabrielfreire.runandlift.feature.student.R

/**
 * "Este código é de fulano. É com ele mesmo?"
 *
 * É o segundo passo do resgate, e ele existe por uma razão que não é conforto: **pedir vínculo é
 * autorizar outra pessoa a ler a própria anamnese** — peso, altura, lesões. Um resgate de um passo
 * só faria isso acontecer no toque seguinte a um erro de digitação, e a leitura do nome já foi paga
 * de qualquer forma.
 *
 * O nome vem de dentro do convite, escrito por quem o gerou. Não é identidade verificada, e é por
 * isso que a frase pergunta em vez de afirmar.
 */
@Composable
internal fun InviteConfirmationCard(
    invite: InviteCode,
    submitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
        ) {
            Text(
                text = invite.trainerName.ifBlank { stringResource(R.string.student_trainer_unnamed) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.student_trainer_confirm_explanation),
                style = MaterialTheme.typography.bodySmall,
            )

            AppButton(
                text = stringResource(R.string.student_trainer_confirm),
                onClick = onConfirm,
                enabled = !submitting,
                loading = submitting,
            )

            AppTextButton(
                text = stringResource(R.string.student_trainer_dismiss),
                onClick = onDismiss,
                enabled = !submitting,
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun InviteConfirmationCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                InviteConfirmationCard(
                    invite = InviteCode(code = "ABC234", trainerId = "treinador-1", trainerName = "Carlos Pereira"),
                    submitting = false,
                    onConfirm = {},
                    onDismiss = {},
                )
            }
        }
    }
}
