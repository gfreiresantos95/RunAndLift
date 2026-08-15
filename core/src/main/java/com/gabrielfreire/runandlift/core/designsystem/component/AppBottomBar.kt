package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Barra de navegação inferior.
 *
 * **O rótulo fica sempre visível** (`alwaysShowLabel`), inclusive na aba não selecionada. O padrão
 * do Material esconde o texto das inativas, o que deixa a barra dependendo só do ícone e do realce
 * de cor para dizer onde se está — dois canais que falham juntos para quem enxerga cor de forma
 * diferente. Mostrar os três rótulos custa alguns dp e é o que o projeto exige (E0-09).
 *
 * **Não decide nada de navegação.** Quem sabe qual aba está ativa e o que acontece ao tocar é a
 * tela; aqui só se desenha o que [items] descreve. É isso que permite a mesma barra servir ao
 * aluno e ao treinador, cujas abas coincidem no formato e divergem no destino.
 *
 * O [NavigationBarItem] do Material 3 já nasce com altura de alvo adequada, então o piso de 48dp do
 * projeto está atendido sem ajuste.
 *
 * **O fundo é `surfaceContainer`, e não `surface`.** Neste esquema `surface` e `background` têm o
 * mesmo valor, então a barra estava exatamente da cor do conteúdo atrás dela: uma faixa de ícones
 * flutuando sem borda, sem separação e sem chão. `surfaceContainer` é o papel que o Material 3
 * reserva justamente para superfícies de navegação, e é o degrau mínimo que separa a barra da tela
 * sem desenhar uma linha para isso.
 *
 * @param items as abas, na ordem em que aparecem. Espera-se de três a cinco — abaixo disso a barra
 *   inferior não se justifica, acima o alvo de toque fica estreito demais.
 */
@Composable
fun AppBottomBar(items: List<AppBottomBarItem>, modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        // Nulo de propósito: o rótulo logo abaixo já é lido pelo TalkBack, e
                        // descrever o ícone faria a aba ser anunciada duas vezes.
                        contentDescription = null,
                    )
                },
                label = { Text(text = item.label) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

/**
 * As três abas com a primeira ativa — é a configuração real do app, e o que se confere aqui é se os
 * rótulos das inativas continuam legíveis nos dois temas.
 */
@LightDarkPreviews
@Composable
private fun AppBottomBarPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppBottomBar(
                items = listOf(
                    AppBottomBarItem(
                        label = PreviewSamples.Tab.HOME,
                        icon = AppIcons.Home,
                        selected = true,
                        onClick = {},
                    ),
                    AppBottomBarItem(
                        label = PreviewSamples.Tab.WORKOUTS,
                        icon = AppIcons.Workouts,
                        selected = false,
                        onClick = {},
                    ),
                    AppBottomBarItem(
                        label = PreviewSamples.Tab.MENU,
                        icon = AppIcons.Menu,
                        selected = false,
                        onClick = {},
                    ),
                ),
            )
        }
    }
}
