package com.gabrielfreire.runandlift.feature.auth.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Chamada, ressalva e as duas saídas — nessa ordem, porque é a ordem em que se lê antes de tocar.
 *
 * As duas saídas vão para a **entrada**, não para o cadastro. Quem instala o app pela primeira vez
 * é minoria em qualquer dia que não seja o do lançamento: a maioria dos toques aqui é de gente que
 * já tem conta, e mandá-la ao cadastro para de lá voltar ao login inverte o caminho comum. O
 * cadastro fica a um toque de distância, no rodapé da entrada, com o mesmo perfil no bolso.
 */
@Composable
internal fun WelcomeRoleChoice(onSelectRole: (ActiveRole) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.welcome_prompt),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXSmall))

        // Antes dos botões, e não depois: quem hesita entre os dois papéis hesita agora, e é agora
        // que saber da reversibilidade destrava a decisão.
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.welcome_reversible),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLarge))

        // Aluno preenchido e treinador contornado: os dois papéis são igualmente válidos, mas há
        // muitos alunos por treinador. A hierarquia segue a população, não uma preferência de
        // produto — e o contorno continua sendo um botão inteiro, não uma opção de segunda classe.
        AppButton(
            text = stringResource(id = R.string.onboarding_student),
            onClick = { onSelectRole(ActiveRole.STUDENT) },
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMedium))

        AppOutlinedButton(
            text = stringResource(id = R.string.onboarding_trainer),
            onClick = { onSelectRole(ActiveRole.TRAINER) },
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))
    }
}

@LightDarkPreviews
@Composable
private fun WelcomeRoleChoicePreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                WelcomeRoleChoice(onSelectRole = {})
            }
        }
    }
}
