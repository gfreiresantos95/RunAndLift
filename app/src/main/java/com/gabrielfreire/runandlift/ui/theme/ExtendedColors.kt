package com.gabrielfreire.runandlift.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Um papel de cor completo, no mesmo formato dos papéis do Material 3.
 *
 * - [color] / [onColor] — para preenchimento sólido (ícone, badge, barra de progresso).
 * - [container] / [onContainer] — para fundo suave (chip, card de alerta, linha de lista).
 */
@Immutable
data class ColorRole(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
)

/**
 * Papéis de cor que o Material 3 não define e que o domínio exige.
 *
 * O semáforo de aderência ([ok], [attention], [critical]) é conceito central do produto: o
 * treinador precisa distinguir, de relance, quem está treinando de quem parou. Por isso vive
 * no tema, e não como cor solta na tela que mostra o painel.
 *
 * **Regra de acessibilidade, não negociável:** cor nunca é o único canal. Todo uso do semáforo
 * acompanha ícone e rótulo textual. Cerca de 8% dos homens têm alguma deficiência de percepção
 * de cor, e o público inclui aluno mais velho e menos digital — o backlog trata isso como
 * requisito (E0-09), não como refinamento.
 *
 * Sobre [critical] e `colorScheme.error`: hoje compartilham os mesmos valores, mas são papéis
 * distintos de propósito. `error` é falha do sistema ou de validação; [critical] é um estado
 * legítimo do aluno. Um pode mudar de tom sem arrastar o outro.
 */
@Immutable
data class ExtendedColorScheme(
    /** Aluno em dia com o programa. Também serve como "sucesso" genérico. */
    val ok: ColorRole,
    /** Aluno escorregando — ainda dá tempo de agir. Também serve como "atenção" genérica. */
    val attention: ColorRole,
    /** Aluno que parou de treinar. Exige ação do treinador. */
    val critical: ColorRole,
    /** Conquista: recorde pessoal, sequência mantida, meta batida. Usar com parcimônia. */
    val highlight: ColorRole,
)

internal val LightExtendedColorScheme = ExtendedColorScheme(
    ok = ColorRole(
        color = Verde40,
        onColor = Neutro100,
        container = Verde90,
        onContainer = Verde10,
    ),
    attention = ColorRole(
        color = Ambar40,
        onColor = Neutro100,
        container = Ambar90,
        onContainer = Ambar10,
    ),
    critical = ColorRole(
        color = Vermelho40,
        onColor = Neutro100,
        container = Vermelho90,
        onContainer = Vermelho10,
    ),
    highlight = ColorRole(
        color = Brasa40,
        onColor = Neutro100,
        container = Brasa90,
        onContainer = Brasa10,
    ),
)

internal val DarkExtendedColorScheme = ExtendedColorScheme(
    ok = ColorRole(
        color = Verde80,
        onColor = Verde20,
        container = Verde30,
        onContainer = Verde90,
    ),
    attention = ColorRole(
        color = Ambar80,
        onColor = Ambar20,
        container = Ambar30,
        onContainer = Ambar90,
    ),
    critical = ColorRole(
        color = Vermelho80,
        onColor = Vermelho20,
        container = Vermelho30,
        onContainer = Vermelho90,
    ),
    highlight = ColorRole(
        color = Brasa80,
        onColor = Brasa20,
        container = Brasa30,
        onContainer = Brasa90,
    ),
)

/**
 * Falha alto (com o esquema claro) se alguém usar `extendedColors` fora de [RunAndLiftTheme]:
 * o valor padrão existe só para o inspetor de preview não quebrar.
 */
internal val LocalExtendedColorScheme = staticCompositionLocalOf { LightExtendedColorScheme }

/** Acesso aos papéis de cor do domínio, no mesmo estilo de `MaterialTheme.colorScheme`. */
val MaterialTheme.extendedColors: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current
