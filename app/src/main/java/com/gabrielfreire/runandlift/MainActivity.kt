package com.gabrielfreire.runandlift

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gabrielfreire.runandlift.core.designsystem.ColorRole
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.core.designsystem.extendedColors

/** Duração do fade da splash. Curto o suficiente para não parecer lentidão de abertura. */
private const val SPLASH_EXIT_DURATION_MS = 250L

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Precisa vir antes de super.onCreate para a Splash Screen API assumir a janela.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Lido a cada frame na thread principal; `.value` evita coletar um Flow só para isso.
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }
        splashScreen.setOnExitAnimationListener { splashProvider ->
            ObjectAnimator.ofFloat(
                splashProvider.view,
                View.ALPHA,
                1f,
                0f,
            ).apply {
                duration = SPLASH_EXIT_DURATION_MS
                // Sem remove() a splash fica congelada sobre o app.
                doOnEnd { splashProvider.remove() }
                start()
            }
        }

        setContent {
            RunAndLiftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold { innerPadding ->
                        FoundationPlaceholder(
                            modifier = Modifier.padding(paddingValues = innerPadding),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tela temporária de fundação: existe para verificar o tema em aparelho real, nos dois modos.
 * Sai quando entrar a primeira tela de verdade.
 */
@Composable
private fun FoundationPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(paddingValues = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = stringResource(R.string.foundation_placeholder_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall)) {
            StatusPill(
                label = stringResource(R.string.adherence_ok),
                role = MaterialTheme.extendedColors.ok,
            )

            StatusPill(
                label = stringResource(R.string.adherence_slipping),
                role = MaterialTheme.extendedColors.attention,
            )

            StatusPill(
                label = stringResource(R.string.adherence_gone),
                role = MaterialTheme.extendedColors.critical,
            )
        }
    }
}

@Composable
private fun StatusPill(label: String, role: ColorRole, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = role.container,
        contentColor = role.onContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = Dimens.SpaceMedium,
                vertical = Dimens.SpaceSmall,
            ),
        )
    }
}

@Preview(name = "Fundação · claro", showBackground = true)
@Preview(
    name = "Fundação · escuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FoundationPlaceholderPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FoundationPlaceholder()
        }
    }
}
