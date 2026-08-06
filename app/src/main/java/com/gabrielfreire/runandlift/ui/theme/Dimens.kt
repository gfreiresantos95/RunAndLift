package com.gabrielfreire.runandlift.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Grade de espaçamento e dimensões mínimas.
 *
 * Não varia entre temas, então é um `object` e não um CompositionLocal — theming aqui seria
 * cerimônia sem ganho.
 */
object Dimens {

    // --- Espaçamento: grade de 4dp ---
    /** 4dp — entre elementos colados (ícone e o seu rótulo). */
    val SpaceXSmall = 4.dp

    /** 8dp — entre itens de um mesmo grupo. */
    val SpaceSmall = 8.dp

    /** 12dp — padding interno de componente compacto. */
    val SpaceMedium = 12.dp

    /** 16dp — padding padrão de tela e de card. */
    val SpaceLarge = 16.dp

    /** 24dp — entre seções distintas. */
    val SpaceXLarge = 24.dp

    /** 32dp — respiro de bloco isolado. */
    val SpaceXXLarge = 32.dp

    /** Padding horizontal padrão de conteúdo de tela. */
    val ScreenPadding = PaddingValues(horizontal = SpaceLarge, vertical = SpaceMedium)

    // --- Alvos de toque ---

    /**
     * 48dp. Piso absoluto para qualquer coisa clicável, exigido pelo backlog (E0-09) e pelas
     * diretrizes de acessibilidade do Android. Vale inclusive para ícone pequeno: o desenho pode
     * ter 24dp, a área de toque não.
     *
     * O caso de uso que justifica: registrar série (E6-02) com a mão suada, em pé, entre
     * repetições. Alvo pequeno aqui não é desconforto, é erro de registro.
     */
    val MinTouchTarget = 48.dp

    /** Alvo confortável para a ação principal de uma tela de execução. */
    val ComfortableTouchTarget = 56.dp

    // --- Raios de canto ---
    val CornerSmall = 8.dp
    val CornerMedium = 12.dp
    val CornerLarge = 16.dp
    val CornerFull = 999.dp

    // --- Elementos ---

    /** Altura de linha de lista com uma linha de texto e ícone. */
    val ListItemHeight = 56.dp

    /** Espessura de divisor e de contorno. */
    val BorderThin = 1.dp

    /** Tamanho de avatar em lista de alunos. */
    val AvatarSmall = 40.dp
    val AvatarMedium = 56.dp
    val AvatarLarge = 96.dp
}

/**
 * Garante que o componente respeite o alvo de toque mínimo, independente do tamanho do desenho.
 *
 * Preferir isto a repetir `.size(48.dp)` em cada ícone clicável: quando o piso mudar, muda num
 * lugar só. Aplicar em qualquer `Modifier.clickable` cujo conteúdo visual seja menor que 48dp.
 */
fun Modifier.minimumTouchTarget(): Modifier =
    this.sizeIn(minWidth = Dimens.MinTouchTarget, minHeight = Dimens.MinTouchTarget)
