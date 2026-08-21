package com.gabrielfreire.runandlift.feature.trainer.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppListRow
import com.gabrielfreire.runandlift.data.model.Program
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * Um programa na lista: o nome e o tamanho dele.
 *
 * A linha de apoio conta **dias e exercícios**, e não a data da última alteração. Quem tem oito
 * programas os distingue pelo que eles são — "3 dias, 18 exercícios" é o formato de um treino ABC —,
 * e não por quando foram mexidos; a data já é o critério de ordenação, e repeti-la em texto gastaria
 * a única linha disponível com o que a posição na lista já disse.
 *
 * Um programa **incompleto se anuncia**: sem nome, sem dia ou com dia vazio, o apoio troca pelo
 * aviso, porque é ele que impede a atribuição, e descobrir isso só na hora de atribuir é descobrir
 * tarde.
 */
@Composable
internal fun ProgramRow(program: Program, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppListRow(
        title = program.name.ifBlank { stringResource(R.string.trainer_program_unnamed) },
        modifier = modifier,
        supportingText = if (program.isAssignable) {
            stringResource(
                R.string.trainer_program_size,
                pluralStringResource(R.plurals.trainer_program_days, program.days.size, program.days.size),
                pluralStringResource(
                    R.plurals.trainer_program_exercises,
                    program.totalExercises,
                    program.totalExercises,
                ),
            )
        } else {
            stringResource(R.string.trainer_program_incomplete)
        },
        onClick = onClick,
    )
}

/**
 * Um programa pronto e um pela metade — o segundo é o estado em que a linha tem algo a dizer, e o
 * que se confere é se o aviso se distingue do apoio comum.
 */
@LightDarkPreviews
@Composable
private fun ProgramRowPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                ProgramRow(program = previewPrograms().first(), onClick = {})
                ProgramRow(program = previewIncompleteProgram(), onClick = {})
            }
        }
    }
}
