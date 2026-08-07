package com.gabrielfreire.runandlift.feature.auth.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.component.AppButton
import com.gabrielfreire.runandlift.core.designsystem.component.AppOutlinedButton
import com.gabrielfreire.runandlift.data.model.ActiveRole
import com.gabrielfreire.runandlift.feature.auth.R

/**
 * Boas-vindas: a primeira tela de quem abre o app sem sessão (E1-02).
 *
 * Ela existe porque o papel não é uma preferência de perfil — é o que decide qual grafo de
 * navegação é montado, o que o cadastro pede e o que a home mostra. Perguntar antes do login
 * permite que o cadastro já saia com o papel gravado, e que ninguém seja perguntado duas vezes
 * pela mesma coisa.
 *
 * **A escolha aqui é intenção, não gravação.** Sem conta não há `uid`, então nada vai ao Firestore
 * nesta tela: o papel viaja como argumento de navegação até o cadastro, que grava depois de
 * autenticar.
 *
 * **A tela não tem estado.** O toque no papel já navega — não há seleção para guardar, botão de
 * confirmação para habilitar, nem ViewModel para segurar coisa alguma. Uma decisão binária com
 * duas saídas visíveis não precisa de um passo de confirmação no meio.
 *
 * O layout separa dois blocos com propósitos diferentes: a marca ocupa **o espaço que sobra**,
 * centralizada nele, e as ações ficam **ancoradas embaixo**, na faixa que o polegar alcança sem
 * reposicionar o aparelho. É o que faz a tela parecer a mesma em telas de tamanhos diferentes:
 * o que estica é o respiro, não a distância até o botão.
 */
@Composable
internal fun WelcomeScreen(onSelectRole: (ActiveRole) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues = Dimens.ScreenPadding),
    ) {
        BrandBlock(modifier = Modifier.weight(1f))
        RoleChoiceBlock(onSelectRole = onSelectRole)
    }
}

/**
 * Marca centralizada no espaço livre.
 *
 * Rola por dentro em vez de empurrar as ações para fora da tela: com a fonte do sistema no
 * tamanho máximo — que o público mais velho usa (E0-09) — o nome e o slogan podem não caber, e o
 * que não pode acontecer é o botão sumir por causa disso.
 */
@Composable
private fun BrandBlock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.welcome_app_name),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSmall))

        Text(
            text = stringResource(id = R.string.welcome_slogan),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Chamada, ressalva e as duas saídas — nessa ordem, porque é a ordem em que se lê antes de tocar. */
@Composable
private fun RoleChoiceBlock(onSelectRole: (ActiveRole) -> Unit, modifier: Modifier = Modifier) {
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

@Preview(name = "Boas-vindas · claro", showBackground = true, heightDp = 800)
@Preview(
    name = "Boas-vindas · escuro",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun WelcomeScreenPreview() {
    RunAndLiftTheme {
        WelcomeScreen(onSelectRole = {})
    }
}
