package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * Mapeamento dos tokens de [Color] para os papéis semânticos do Material 3.
 *
 * Os dois esquemas são espelhados: o que é tom 40 no claro é tom 80 no escuro, o container
 * troca de 90 para 30. Manter essa simetria é o que garante que uma tela escrita contra
 * `colorScheme` funcione nos dois temas sem ajuste manual.
 *
 * Contraste: todo par `on<Papel>` / `<Papel>` foi escolhido para ficar acima de 4.5:1
 * (WCAG AA para texto normal). Ao alterar um tom, verifique o par correspondente — o
 * requisito de contraste AA é decisão de produto registrada no backlog (E0-09), não
 * preferência estética.
 */
internal val LightColorScheme = lightColorScheme(
    primary = Cobalto40,
    onPrimary = Neutro100,
    primaryContainer = Cobalto90,
    onPrimaryContainer = Cobalto10,
    inversePrimary = Cobalto80,
    secondary = Aco40,
    onSecondary = Neutro100,
    secondaryContainer = Aco90,
    onSecondaryContainer = Aco10,
    tertiary = Brasa40,
    onTertiary = Neutro100,
    tertiaryContainer = Brasa90,
    onTertiaryContainer = Brasa10,
    error = Vermelho40,
    onError = Neutro100,
    errorContainer = Vermelho90,
    onErrorContainer = Vermelho10,
    background = Neutro98,
    onBackground = Neutro10,
    surface = Neutro98,
    onSurface = Neutro10,
    surfaceVariant = NeutroVariante90,
    onSurfaceVariant = NeutroVariante30,
    surfaceTint = Cobalto40,
    inverseSurface = Neutro20,
    inverseOnSurface = Neutro96,
    surfaceDim = Neutro87,
    surfaceBright = Neutro98,
    surfaceContainerLowest = Neutro100,
    surfaceContainerLow = Neutro96,
    surfaceContainer = Neutro94,
    surfaceContainerHigh = Neutro92,
    surfaceContainerHighest = Neutro90,
    outline = NeutroVariante50,
    outlineVariant = NeutroVariante80,
    scrim = Neutro0,
)

internal val DarkColorScheme = darkColorScheme(
    primary = Cobalto80,
    onPrimary = Cobalto20,
    primaryContainer = Cobalto30,
    onPrimaryContainer = Cobalto90,
    inversePrimary = Cobalto40,
    secondary = Aco80,
    onSecondary = Aco20,
    secondaryContainer = Aco30,
    onSecondaryContainer = Aco90,
    tertiary = Brasa80,
    onTertiary = Brasa20,
    tertiaryContainer = Brasa30,
    onTertiaryContainer = Brasa90,
    error = Vermelho80,
    onError = Vermelho20,
    errorContainer = Vermelho30,
    onErrorContainer = Vermelho90,
    background = Neutro8,
    onBackground = Neutro90,
    surface = Neutro8,
    onSurface = Neutro90,
    surfaceVariant = NeutroVariante30,
    onSurfaceVariant = NeutroVariante80,
    surfaceTint = Cobalto80,
    inverseSurface = Neutro90,
    inverseOnSurface = Neutro20,
    surfaceDim = Neutro8,
    surfaceBright = Neutro24,
    surfaceContainerLowest = Neutro4,
    surfaceContainerLow = Neutro10,
    surfaceContainer = Neutro12,
    surfaceContainerHigh = Neutro17,
    surfaceContainerHighest = Neutro22,
    outline = NeutroVariante60,
    outlineVariant = NeutroVariante30,
    scrim = Neutro0,
)
