package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Durações, curvas e transições de tela.
 *
 * Existe porque movimento é a parte da interface que mais denuncia descuido e a que menos costuma
 * ter dono: sem um lugar para ele, cada tela inventa a sua duração, e o app inteiro fica com o ritmo
 * irregular. Aqui ficam os valores; as telas consomem.
 *
 * As durações e as curvas são as do Material 3, e não números escolhidos a olho. Vale saber por quê:
 *
 * - **[Emphasized]** é a curva das transições que a pessoa **pediu** — trocar de tela, abrir uma
 *   folha. Ela sai devagar, acelera e freia longo, que é o que dá a sensação de peso: o objeto tem
 *   massa, e não teleporta.
 * - **[Standard]** é a das mudanças pequenas que acontecem **dentro** de uma tela, onde o movimento
 *   não pode chamar atenção para si.
 * - **[EmphasizedDecelerate]** é a de quem entra, e **[EmphasizedAccelerate]** a de quem sai. Entrar
 *   devagar no fim e sair rápido no começo é o que faz a tela nova parecer chegar, em vez de a
 *   antiga parecer ter sumido.
 *
 * **A navegação usa eixo compartilhado horizontal** ([forwardEnter] e companhia): a tela nova entra
 * pela direita e a anterior sai pela esquerda, e o inverso ao voltar. A alternativa que estava em
 * uso — o esmaecimento padrão do Compose Navigation, de 700 ms — tem dois problemas. É lenta o
 * bastante para ser percebida como travamento, e **não tem direção**: sem o deslocamento lateral,
 * ir e voltar são o mesmo movimento, e a pessoa perde a noção de onde está na pilha.
 *
 * O deslocamento é uma **fração da largura** ([SLIDE_FRACTION]) e não um valor fixo em dp: em tela
 * grande, 30 dp de deslocamento somem, e o movimento vira só um esmaecimento caro.
 */
object AppMotion {

    /** Mudança pequena dentro de uma tela — um campo que aparece, um chip que muda de cor. */
    const val DURATION_SHORT: Int = 150

    /** Troca de tela e mudanças de layout que a pessoa acompanha com o olho. */
    const val DURATION_MEDIUM: Int = 300

    /** Reservada ao que precisa ser notado sem ser lido — hoje, nada. Usar com parcimônia. */
    const val DURATION_LONG: Int = 450

    /** Curva das transições pedidas pela pessoa. */
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Curva de quem entra em cena. */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Curva de quem sai de cena. */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Curva do que muda dentro de uma tela sem pedir atenção. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Avançando: a tela nova entra pela direita. */
    val forwardEnter: EnterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = EmphasizedDecelerate),
        initialOffsetX = { width -> width / SLIDE_FRACTION },
    ) + fadeIn(animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = Standard))

    /** Avançando: a tela anterior sai pela esquerda. */
    val forwardExit: ExitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = EmphasizedAccelerate),
        targetOffsetX = { width -> -width / SLIDE_FRACTION },
    ) + fadeOut(animationSpec = tween(durationMillis = DURATION_SHORT, easing = Standard))

    /** Voltando: a tela anterior volta pela esquerda. */
    val backEnter: EnterTransition = slideInHorizontally(
        animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = EmphasizedDecelerate),
        initialOffsetX = { width -> -width / SLIDE_FRACTION },
    ) + fadeIn(animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = Standard))

    /** Voltando: a tela atual sai pela direita, por onde entrou. */
    val backExit: ExitTransition = slideOutHorizontally(
        animationSpec = tween(durationMillis = DURATION_MEDIUM, easing = EmphasizedAccelerate),
        targetOffsetX = { width -> width / SLIDE_FRACTION },
    ) + fadeOut(animationSpec = tween(durationMillis = DURATION_SHORT, easing = Standard))

    /**
     * Fração da largura que a tela percorre.
     *
     * Um décimo, e não a largura inteira: o eixo compartilhado do Material sugere movimento, não
     * mudança de página. Deslizar a tela toda a cada toque cansa em um aplicativo em que se navega
     * dezenas de vezes por sessão.
     */
    private const val SLIDE_FRACTION = 10
}
