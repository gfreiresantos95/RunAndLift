package com.gabrielfreire.runandlift.feature.student.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme
import com.gabrielfreire.runandlift.feature.student.R
import com.gabrielfreire.runandlift.feature.student.text.fullLabel
import com.gabrielfreire.runandlift.feature.student.text.shortLabel
import java.time.DayOfWeek

/**
 * A semana em sete marcas: o que foi treinado, o que está previsto e o que é descanso.
 *
 * É a peça que o mercado inteiro convergiu para ter, e a razão é sempre a mesma: constância se
 * enxerga em faixa, não em número. "3 de 4 treinos" é uma nota; sete marcas lado a lado mostram
 * *quais* dias, e é olhando para elas que alguém percebe que sempre falha na sexta.
 *
 * **Três canais para três estados, e nenhum deles é só a cor.** Cumprido leva o visto sobre a cor
 * da marca, previsto leva o contorno vazio, descanso leva o traço sobre o cinza — o mesmo par de
 * sinais dos chips de escolha, pela mesma razão (E0-09). Cada um dos sete dias tem uma marca
 * visível: um dia desenhado com a cor do fundo é um buraco na faixa, e quem olha conta seis.
 *
 * Cada dia é um nó só para o leitor de tela, e o nome vai **por extenso**: "Seg" lido em voz alta é
 * sopa de letras, e sete delas seguidas não formam uma semana na cabeça de ninguém.
 */
@Composable
internal fun WeekStrip(dashboard: StudentDashboard, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        dashboard.week.forEach { (day, state) -> DayMark(day = day, state = state) }
    }
}

/** Um dia: a abreviação em cima e a marca embaixo. */
@Composable
private fun DayMark(day: DayOfWeek, state: TrainingDayState) {
    val description = state.description(day = day.fullLabel())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Text(
            text = day.shortLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DayBadge(state = state)
    }
}

/**
 * A marca do dia: cheia com visto, vazia com contorno, ou cinza com traço.
 *
 * **Nenhum dia fica em branco.** O descanso era uma marca da mesma cor do fundo, o que numa faixa
 * de sete a fazia sumir — e sete posições em que três são invisíveis não se leem como uma semana,
 * se leem como uma linha torta. Agora ele tem peso próprio, e é o desenho mais apagado dos três,
 * porque é o único que não pede nada de quem lê.
 */
@Composable
private fun DayBadge(state: TrainingDayState) {
    Surface(
        modifier = Modifier.size(size = Dimens.AvatarSmall),
        shape = MaterialTheme.shapes.small,
        color = state.badgeColor(),
        contentColor = state.badgeContentColor(),
        border = state.badgeBorder(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            state.badgeIcon()?.let { icon ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(size = Dimens.IconSmall),
                )
            }
        }
    }
}

/**
 * O desenho dentro da marca, ou `null` no dia previsto.
 *
 * O previsto é o único sem ícone de propósito: ele é o dia que **ainda não aconteceu**, e qualquer
 * desenho ali afirmaria alguma coisa sobre um dia que não terminou. Cumprido e descanso são fatos,
 * e fato tem símbolo.
 */
private fun TrainingDayState.badgeIcon(): Int? = when (this) {
    TrainingDayState.DONE -> AppIcons.Check
    TrainingDayState.PLANNED -> null
    TrainingDayState.REST -> AppIcons.Rest
}

/** A frase que o leitor de tela diz no lugar da marca. */
@Composable
private fun TrainingDayState.description(day: String): String = stringResource(
    when (this) {
        TrainingDayState.DONE -> R.string.student_home_week_day_done
        TrainingDayState.PLANNED -> R.string.student_home_week_day_planned
        TrainingDayState.REST -> R.string.student_home_week_day_rest
    },
    day,
)

/**
 * O fundo da marca.
 *
 * O descanso é `surfaceVariant`, e não vermelho: vermelho é a cor de erro do tema, e usá-la no dia
 * que o programa mandou descansar acusa de falta justamente quem está seguindo o plano. Quando
 * existir treino registrado de verdade, o dia previsto que passou em branco é que será a peça a
 * pedir uma cor de aviso — e aí ela vai significar alguma coisa.
 */
@Composable
private fun TrainingDayState.badgeColor() = when (this) {
    TrainingDayState.DONE -> MaterialTheme.colorScheme.primary
    TrainingDayState.PLANNED -> MaterialTheme.colorScheme.surface
    TrainingDayState.REST -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun TrainingDayState.badgeContentColor() = when (this) {
    TrainingDayState.DONE -> MaterialTheme.colorScheme.onPrimary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun TrainingDayState.badgeBorder() = when (this) {
    TrainingDayState.PLANNED ->
        BorderStroke(width = Dimens.BorderThin, color = MaterialTheme.colorScheme.outline)

    else -> null
}

/**
 * A semana de exemplo: três cumpridos, um previsto em aberto e três de descanso — que é a única
 * combinação em que os três estados aparecem juntos, e portanto a única que serve de conferência.
 */
@LightDarkPreviews
@Composable
private fun WeekStripPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(all = Dimens.SpaceLarge)) {
                WeekStrip(dashboard = StudentDashboard.SAMPLE)
            }
        }
    }
}
