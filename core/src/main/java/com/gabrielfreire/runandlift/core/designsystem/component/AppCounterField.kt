package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabrielfreire.runandlift.core.designsystem.AppIcons
import com.gabrielfreire.runandlift.core.designsystem.Dimens
import com.gabrielfreire.runandlift.core.designsystem.LightDarkPreviews
import com.gabrielfreire.runandlift.core.designsystem.MetricTextStyles
import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples
import com.gabrielfreire.runandlift.core.designsystem.RunAndLiftTheme

/**
 * Número que se ajusta com − e +, sem teclado.
 *
 * Nasce da prescrição de treino, onde se preenchem cinco números seguidos — séries, mínimo e máximo
 * de repetições, carga, descanso — e se repete isso quarenta vezes num programa. **Teclado numérico
 * é o pior jeito de fazer isso**: ele cobre metade da tela, some e volta a cada campo, e o valor
 * quase sempre está a um ou dois passos do que já está lá. Dois botões de 48 dp resolvem em um
 * toque o que o teclado resolve em quatro.
 *
 * **O valor é texto, não campo editável.** Não dá para digitar 200 séries por engano, e não há
 * estado intermediário vazio para validar — que é a metade do trabalho de um campo numérico comum.
 * O preço é não conseguir saltar de 3 para 60 direto; por isso [step] existe, e é por isso que o
 * descanso anda de 15 em 15 em vez de 1 em 1.
 *
 * O número usa [MetricTextStyles], como todo número medido do app: sem dígitos tabulares, a linha
 * inteira dança quando o valor passa de 9 para 10 com o dedo ainda no botão.
 *
 * **Um nó só para o leitor de tela.** Sem isso, o TalkBack lê "menos, botão", "4", "mais, botão" e
 * deixa quem escuta montar a frase sozinho; com isso, lê "Séries, 4".
 *
 * @param value o valor atual. Quem o mantém dentro de [range] é quem chama — este componente só
 *   desabilita o botão que sairia da faixa.
 * @param step de quanto em quanto o botão anda. 1 para séries e repetições, 15 para segundos de
 *   descanso, 5 para quilos.
 * @param suffix unidade colada ao número — "kg", "s". Fica fora do estilo de dígito tabular de
 *   propósito: é palavra, não medida.
 * @param decrementLabel e @param incrementLabel o que o leitor de tela diz de cada botão. Chegam de
 *   fora porque `:core` não tem `strings.xml` por decisão.
 */
@Composable
fun AppCounterField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    decrementLabel: String,
    incrementLabel: String,
    modifier: Modifier = Modifier,
    range: IntRange = DEFAULT_RANGE,
    step: Int = 1,
    suffix: String? = null,
) {
    val canDecrement = value - step >= range.first
    val canIncrement = value + step <= range.last
    val reading = suffix?.let { "$label, $value $it" } ?: "$label, $value"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXSmall),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSmall),
            modifier = Modifier.clearAndSetSemantics { contentDescription = reading },
        ) {
            StepButton(
                icon = AppIcons.Rest,
                description = decrementLabel,
                enabled = canDecrement,
                onClick = { onValueChange(value - step) },
            )

            Text(
                text = suffix?.let { "$value $it" } ?: value.toString(),
                style = MetricTextStyles.medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = VALUE_MIN_WIDTH),
            )

            StepButton(
                icon = AppIcons.Add,
                description = incrementLabel,
                enabled = canIncrement,
                onClick = { onValueChange(value + step) },
            )
        }
    }
}

/**
 * Um dos dois botões.
 *
 * `IconButton` já entrega os 48 dp de alvo — o desenho tem 24, a área de toque não, que é a regra do
 * projeto (E0-09).
 */
@Composable
private fun StepButton(icon: Int, description: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(Dimens.MinTouchTarget),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(painter = painterResource(icon), contentDescription = description)
        }
    }
}

/**
 * A faixa padrão, e ela não começa em zero.
 *
 * Um exercício com zero séries não é uma prescrição leve, é a ausência de uma — e quem quis remover
 * o exercício tem o botão de remover para isso. Deixar o contador chegar a zero cria um estado que
 * a tela teria de interpretar toda vez.
 */
private val DEFAULT_RANGE = 1..99

/** Largura mínima do número, para os dois botões não andarem quando ele passa de 9 para 10. */
private val VALUE_MIN_WIDTH = 56.dp

@LightDarkPreviews
@Composable
private fun AppCounterFieldPreview() {
    RunAndLiftTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(all = Dimens.SpaceLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLarge),
            ) {
                // No mínimo da faixa: é onde se confere que o botão de diminuir fica desabilitado
                // em vez de deixar o valor cair para zero.
                AppCounterField(
                    value = 1,
                    onValueChange = {},
                    label = PreviewSamples.Prescription.SETS,
                    decrementLabel = PreviewSamples.Prescription.DECREMENT,
                    incrementLabel = PreviewSamples.Prescription.INCREMENT,
                )
                AppCounterField(
                    value = 90,
                    onValueChange = {},
                    label = PreviewSamples.Prescription.REST,
                    decrementLabel = PreviewSamples.Prescription.DECREMENT,
                    incrementLabel = PreviewSamples.Prescription.INCREMENT,
                    range = 0..600,
                    step = 15,
                    suffix = PreviewSamples.Prescription.SECONDS,
                )
            }
        }
    }
}
