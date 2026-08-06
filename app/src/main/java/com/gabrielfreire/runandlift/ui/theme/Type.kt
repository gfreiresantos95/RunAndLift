package com.gabrielfreire.runandlift.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica do app.
 *
 * Usa [FontFamily.Default] de propósito: a fonte do sistema já vem otimizada para a tela do
 * usuário, respeita a preferência de fonte do dispositivo e não custa nada no APK. Trocar por
 * uma fonte de marca é decisão posterior — quando vier, muda só este arquivo.
 *
 * Regras da escala:
 * - **14sp é o piso para conteúdo.** Nada abaixo disso carrega informação que o usuário precise
 *   ler. 12sp existe apenas para rótulo de apoio.
 * - **Tudo em `sp`**, nunca `dp`, para que a escala de fonte do sistema funcione. O público
 *   inclui aluno mais velho que aumenta a fonte do celular (backlog E0-09 / D11).
 * - `lineHeightStyle` com recorte de espaço extra deixa o texto opticamente centralizado nos
 *   componentes compactos, que é onde a maior parte desta UI vive.
 */
private val DefaultLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun appTextStyle(
    fontSize: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double,
): TextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = DefaultLineHeightStyle,
)

val AppTypography = Typography(
    displayLarge = appTextStyle(
        fontSize = 57,
        lineHeight = 64,
        weight = FontWeight.Normal,
        letterSpacing = -0.25
    ),
    displayMedium = appTextStyle(
        fontSize = 45,
        lineHeight = 52,
        weight = FontWeight.Normal,
        letterSpacing = 0.0
    ),
    displaySmall = appTextStyle(
        fontSize = 36,
        lineHeight = 44,
        weight = FontWeight.Normal,
        letterSpacing = 0.0
    ),
    headlineLarge = appTextStyle(
        fontSize = 32,
        lineHeight = 40,
        weight = FontWeight.SemiBold,
        letterSpacing = 0.0
    ),
    headlineMedium = appTextStyle(
        fontSize = 28,
        lineHeight = 36,
        weight = FontWeight.SemiBold,
        letterSpacing = 0.0
    ),
    headlineSmall = appTextStyle(
        fontSize = 24,
        lineHeight = 32,
        weight = FontWeight.SemiBold,
        letterSpacing = 0.0
    ),
    titleLarge = appTextStyle(
        fontSize = 22,
        lineHeight = 28,
        weight = FontWeight.SemiBold,
        letterSpacing = 0.0
    ),
    titleMedium = appTextStyle(
        fontSize = 16,
        lineHeight = 24,
        weight = FontWeight.Medium,
        letterSpacing = 0.15
    ),
    titleSmall = appTextStyle(
        fontSize = 14,
        lineHeight = 20,
        weight = FontWeight.Medium,
        letterSpacing = 0.1
    ),
    bodyLarge = appTextStyle(
        fontSize = 16,
        lineHeight = 24,
        weight = FontWeight.Normal,
        letterSpacing = 0.5
    ),
    bodyMedium = appTextStyle(
        fontSize = 14,
        lineHeight = 20,
        weight = FontWeight.Normal,
        letterSpacing = 0.25
    ),
    bodySmall = appTextStyle(
        fontSize = 12,
        lineHeight = 16,
        weight = FontWeight.Normal,
        letterSpacing = 0.4
    ),
    labelLarge = appTextStyle(
        fontSize = 14,
        lineHeight = 20,
        weight = FontWeight.Medium,
        letterSpacing = 0.1
    ),
    labelMedium = appTextStyle(
        fontSize = 12,
        lineHeight = 16,
        weight = FontWeight.Medium,
        letterSpacing = 0.5
    ),
    labelSmall = appTextStyle(
        fontSize = 11,
        lineHeight = 16,
        weight = FontWeight.Medium,
        letterSpacing = 0.5
    ),
)

/**
 * Estilos para números medidos — carga, repetição, RPE, percentual de aderência.
 *
 * Existem por dois motivos que a escala do Material não resolve:
 * 1. **Dígitos tabulares** (`tnum`): sem isso, trocar 1 por 8 muda a largura do número e a
 *    linha "dança" a cada série registrada. Numa tela de execução de treino, onde o mesmo campo
 *    é atualizado dezenas de vezes, isso é ruído visual constante.
 * 2. **Peso e tamanho maiores que o corpo de texto**: o número é o dado, e precisa ser lido de
 *    relance por alguém em pé, no meio de uma série, com o celular a meio metro do rosto.
 *
 * Não são parte do [Typography] do Material porque não substituem nenhum papel dele — são
 * adicionais. Não variam entre tema claro e escuro, então não precisam de CompositionLocal.
 */
object MetricTextStyles {
    private const val TABULAR_FIGURES = "\"tnum\" 1"

    /** Número protagonista da tela: carga da série em execução, aderência no painel. */
    val large: TextStyle =
        appTextStyle(fontSize = 40, lineHeight = 48, weight = FontWeight.Bold, letterSpacing = 0.0)
            .copy(fontFeatureSettings = TABULAR_FIGURES)

    /** Número em lista ou card: carga da série anterior, total de treinos. */
    val medium: TextStyle = appTextStyle(
        fontSize = 24,
        lineHeight = 32,
        weight = FontWeight.SemiBold,
        letterSpacing = 0.0
    ).copy(fontFeatureSettings = TABULAR_FIGURES)

    /** Número embutido em texto corrido ou em chip compacto. */
    val small: TextStyle = appTextStyle(
        fontSize = 16,
        lineHeight = 24,
        weight = FontWeight.Medium,
        letterSpacing = 0.0
    ).copy(fontFeatureSettings = TABULAR_FIGURES)
}
