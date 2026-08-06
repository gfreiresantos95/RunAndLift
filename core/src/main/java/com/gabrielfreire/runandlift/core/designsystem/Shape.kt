package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas do design system, derivadas dos raios de [Dimens] para não haver duas fontes de verdade
 * de arredondamento.
 *
 * `extraLarge` fica em 28dp (e não no raio de [Dimens]) porque é o valor que os componentes
 * grandes do Material 3 — bottom sheet, diálogo — já esperam.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(size = Dimens.CornerSmall / 2),
    small = RoundedCornerShape(size = Dimens.CornerSmall),
    medium = RoundedCornerShape(size = Dimens.CornerMedium),
    large = RoundedCornerShape(size = Dimens.CornerLarge),
    extraLarge = RoundedCornerShape(size = 28.dp),
)
