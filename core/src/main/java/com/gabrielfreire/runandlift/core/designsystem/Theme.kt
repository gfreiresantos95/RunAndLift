package com.gabrielfreire.runandlift.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tema raiz do app. Envolve toda a UI, nos dois papéis (treinador e aluno).
 *
 * **Sem cor dinâmica (Material You), de propósito.** O produto depende de cor com significado
 * estável: o semáforo de aderência (ver [ExtendedColorScheme]) precisa ser o mesmo verde,
 * âmbar e vermelho no aparelho de todo mundo, e a identidade da marca não pode virar o papel de
 * parede de quem instalou. Cor dinâmica repintaria os dois. Se algum dia isso for reconsiderado,
 * a decisão tem que resolver antes o que acontece com os papéis de estado.
 *
 * @param darkTheme segue o sistema por padrão. O parâmetro existe para preview e teste; a
 *   preferência explícita de tema dentro do app é item de Fase 2 do backlog (E0-12) e, quando
 *   vier, entra como uma fonte de valor para este parâmetro — não como um tema paralelo.
 */
@Composable
fun RunAndLiftTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColorScheme = if (darkTheme) DarkExtendedColorScheme else LightExtendedColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            /**
             * Em edge-to-edge o app desenha atrás das barras do sistema, então os ícones delas
             * (hora, bateria, sinal) precisam inverter junto com o tema — senão vira ícone
             * branco sobre superfície clara no tema claro.
             */
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(value = LocalExtendedColorScheme provides extendedColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
