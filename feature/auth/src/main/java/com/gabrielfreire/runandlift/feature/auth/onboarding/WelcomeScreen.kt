package com.gabrielfreire.runandlift.feature.auth.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.data.model.ActiveRole

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
 * O layout separa dois blocos com propósitos diferentes: [WelcomeBrand] ocupa **o espaço que
 * sobra**, e [WelcomeRoleChoice] fica **ancorado embaixo**, na faixa que o polegar alcança sem
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
        WelcomeBrand(modifier = Modifier.weight(1f))
        WelcomeRoleChoice(onSelectRole = onSelectRole)
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
