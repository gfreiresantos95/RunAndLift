package com.gabrielfreire.runandlift.feature.trainer.home

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.ColorRole
import com.gabrielfreire.runandlift.core.designsystem.extendedColors
import com.gabrielfreire.runandlift.feature.trainer.R

/**
 * O semáforo de aderência aplicado a uma pessoa: em dia, escorregando, parou.
 *
 * É o conceito central do produto do lado do treinador, e o que o mercado inteiro chama de
 * *at-risk client*: o valor de um painel de treinador não está em somar treinos, está em dizer de
 * quem ele precisa cuidar esta semana. Uma lista de trinta nomes em ordem alfabética não faz isso;
 * três nomes com o motivo ao lado fazem.
 *
 * [OK] existe embora ninguém precise de atenção estando em dia: é o terceiro estado que dá sentido
 * aos outros dois, e é o que o painel mostra quando a semana correu bem — a alternativa seria um
 * bloco que só aparece quando há problema, e que por isso nunca ensina o que é o normal.
 *
 * As três traduções — palavra, cor e ícone — moram neste arquivo, junto do enum, e não num arquivo
 * de mensagens distante: assim acrescentar um quarto nível quebra o `when` na linha de baixo, e não
 * em outro lugar do módulo.
 */
internal enum class AttentionLevel {

    /** Treinando como combinado. */
    OK,

    /** Falhou treinos, mas ainda está por perto. É onde uma mensagem ainda resolve. */
    SLIPPING,

    /** Parou. Passou do ponto em que uma cobrança resolve, e vira conversa. */
    STOPPED,
}

/** A palavra, que é o canal que funciona para todo mundo. */
@Composable
internal fun AttentionLevel.label(): String = stringResource(
    when (this) {
        AttentionLevel.OK -> R.string.trainer_home_level_ok
        AttentionLevel.SLIPPING -> R.string.trainer_home_level_slipping
        AttentionLevel.STOPPED -> R.string.trainer_home_level_stopped
    },
)

/** A cor, que é o canal mais rápido e o menos confiável — nunca vai sozinha. */
@Composable
internal fun AttentionLevel.role(): ColorRole = when (this) {
    AttentionLevel.OK -> MaterialTheme.extendedColors.ok
    AttentionLevel.SLIPPING -> MaterialTheme.extendedColors.attention
    AttentionLevel.STOPPED -> MaterialTheme.extendedColors.critical
}

/**
 * O desenho. Só quem está em dia leva o visto; os outros dois levam o mesmo sinal de atenção, e o
 * que os separa é a cor e a palavra — três desenhos distintos para três graus da mesma coisa
 * exigiriam decorar um vocabulário de ícones que ninguém pediu para aprender.
 */
@DrawableRes
internal fun AttentionLevel.icon(): Int = when (this) {
    AttentionLevel.OK -> AppIcons.Check
    else -> AppIcons.Alert
}
