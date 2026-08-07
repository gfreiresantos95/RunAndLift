package com.gabrielfreire.runandlift

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.rememberNavController
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.navigation.RunAndLiftNavHost
import com.gabrielfreire.runandlift.navigation.navigateToRole

/** Duração do fade da splash. Curto o suficiente para não parecer lentidão de abertura. */
private const val SPLASH_EXIT_DURATION_MS = 250L

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val container = (application as RunAndLiftApplication).container
        viewModelFactory {
            initializer {
                MainViewModel(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Precisa vir antes de super.onCreate para a Splash Screen API assumir a janela.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Lido a cada frame na thread principal; `.value` evita coletar um Flow só para isso.
        splashScreen.setKeepOnScreenCondition { !viewModel.uiState.value.ready }
        splashScreen.setOnExitAnimationListener { splashProvider ->
            ObjectAnimator.ofFloat(splashProvider.view, View.ALPHA, 1f, 0f).apply {
                duration = SPLASH_EXIT_DURATION_MS
                // Sem remove() a splash fica congelada sobre o app.
                doOnEnd { splashProvider.remove() }
                start()
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val container = (application as RunAndLiftApplication).container
            val navController = rememberNavController()

            RunAndLiftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Só monta o grafo depois de o destino inicial estar decidido: montar antes
                    // significaria começar no login e trocar de tela um frame adiante.
                    if (state.ready) {
                        Scaffold { innerPadding ->
                            RunAndLiftNavHost(
                                startDestination = state.startDestination,
                                authRepository = container.authRepository,
                                userRepository = container.userRepository,
                                canSwitchRole = state.canSwitchRole,
                                onSwitchRole = {
                                    viewModel.switchRole { role ->
                                        navController.navigateToRole(role, clearAuth = false)
                                    }
                                },
                                navController = navController,
                                modifier = Modifier.padding(paddingValues = innerPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}
