package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
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
    val ScreenPadding = PaddingValues(horizontal = SpaceLarge, vertical = SpaceSmall)

    /**
     * Largura máxima de uma coluna de conteúdo — formulário, texto corrido, lista de opções.
     *
     * 600dp porque acima disso a linha passa de setenta e cinco caracteres, que é onde a leitura
     * começa a falhar: o olho perde o início da linha seguinte ao voltar. Num telefone o valor nunca
     * é alcançado e nada muda; num tablet, num dobrável aberto ou numa janela redimensionada é o que
     * impede o formulário de esticar de ponta a ponta e virar uma faixa de campos com dois palmos de
     * distância entre o rótulo e o valor.
     *
     * Quem aplica é `AppScreenColumn`; nenhuma tela precisa se lembrar do número.
     */
    val ContentMaxWidth = 600.dp

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

/**
 * Limita a largura a [Dimens.ContentMaxWidth], ocupando tudo o que couber abaixo disso.
 *
 * **Centralizar é do pai** — este modificador só limita. Na prática ninguém o usa direto: quem
 * combina as duas coisas é `AppScreenColumn`, que é onde a coluna de conteúdo das telas mora.
 */
fun Modifier.contentWidth(): Modifier = this
    .widthIn(max = Dimens.ContentMaxWidth)
    .fillMaxWidth()
