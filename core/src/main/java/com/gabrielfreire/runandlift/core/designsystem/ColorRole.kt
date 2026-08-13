package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Um papel de cor completo, no mesmo formato dos papéis do Material 3.
 *
 * - [color] / [onColor] — para preenchimento sólido (ícone, badge, barra de progresso).
 * - [container] / [onContainer] — para fundo suave (chip, card de alerta, linha de lista).
 *
 * Fica separado de [ExtendedColorScheme] porque é a **forma** de um papel de cor, e não a lista de
 * papéis que o produto tem: acrescentar um papel ao domínio mexe lá, mudar o que compõe um papel
 * mexe aqui.
 */
@Immutable
data class ColorRole(val color: Color, val onColor: Color, val container: Color, val onContainer: Color)
