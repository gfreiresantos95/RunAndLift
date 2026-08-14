package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors
import com.gabrielfreire.runandlift.feature.student.R

/**
 * O aviso de perfil incompleto, na home do aluno.
 *
 * É a segunda chance do que o onboarding deixou pular — e por isso **avisa, não bloqueia**. Nada do
 * que falta impede alguém de treinar, e um bloqueio aqui puniria justamente quem preferiu começar
 * antes de responder tudo.
 *
 * Usa o papel `attention` do tema, e não `critical`: cadastro incompleto não é falha nem erro, é
 * uma pendência. Pintá-lo de vermelho ensinaria a ignorar o vermelho que importa.
 *
 * O texto diz **quantas** perguntas faltam. "Complete seu cadastro" sozinho não deixa a pessoa
 * decidir se agora é hora; "faltam 2 respostas" deixa.
 *
 * O card inteiro é o alvo de toque, e não um link dentro dele: é uma ação só, e um alvo do tamanho
 * do card é o que se acerta com a mão suada, em pé, dentro da academia.
 */
@Composable
internal fun ProfileReminderCard(missingCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val attention = MaterialTheme.extendedColors.attention

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        color = attention.container,
        contentColor = attention.onContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(all = Dimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        ) {
            Text(
                text = pluralStringResource(R.plurals.student_home_reminder_title, missingCount, missingCount),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.student_home_reminder_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** Com uma pendência e com várias — é o plural que se confere aqui. */
@LightDarkPreviews
@Composable
private fun ProfileReminderCardPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            ) {
                ProfileReminderCard(missingCount = 1, onClick = {})
                ProfileReminderCard(missingCount = 4, onClick = {})
            }
        }
    }
}
