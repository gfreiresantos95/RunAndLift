package com.gabrielfreire.runandlift.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Como um item selecionável se veste, marcado e desmarcado.
 *
 * Existe porque o onboarding tinha **três visuais de seleção em três passos seguidos**: o nível
 * usava cartão cinza que virava roxo, os dias usavam quadrado cinza que virava azul, e as lesões
 * usavam chip com contorno que virava preenchido. Três respostas para a mesma pergunta — "isto está
 * escolhido?" — obrigam a pessoa a reaprender o código de cor a cada tela, e num passo a passo elas
 * vêm uma atrás da outra.
 *
 * A resposta única é a do chip do Material 3, que é a mais legível das três:
 *
 * - **Desmarcado é contorno sobre nada.** Sem preenchimento, o item não compete com o que estiver
 *   selecionado ao lado dele. Uma lista de nove opções cinzas preenchidas é uma parede; uma lista de
 *   nove contornos é uma lista.
 * - **Marcado é preenchido, sem contorno.** A troca de *presença de preenchimento* é o que faz a
 *   diferença sobreviver em escala de cinza — e é isso que atende a regra do projeto de a cor nunca
 *   ser o único canal (E0-09). Não é a cor que muda: é o item deixar de ser um contorno e passar a
 *   ser um bloco.
 *
 * O contorno não muda o tamanho do item ao aparecer e sumir: `Surface` desenha a borda para dentro
 * dos limites. Sem isso, a lista inteira daria um pulo de 2 dp a cada toque.
 *
 * **Não inclui ícone de confirmação.** O chip de lesão tem um, e é escolha dele: lá a seleção é
 * múltipla e o visto reforça "mais este". Onde a escolha é uma só — nível, objetivo, dia — o visto
 * seria ruído repetido em cada linha.
 */
@Immutable
data class SelectionAppearance(
    val container: Color,
    val content: Color,
    /** `null` quando marcado — o preenchimento já delimita o item, e a borda o engrossaria à toa. */
    val border: BorderStroke?,
)

/** A aparência do estado atual. Consumir sempre daqui, e nunca escolher a cor na tela. */
@Composable
@ReadOnlyComposable
fun selectionAppearance(selected: Boolean): SelectionAppearance = if (selected) {
    SelectionAppearance(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
        border = null,
    )
} else {
    SelectionAppearance(
        container = Color.Transparent,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(width = Dimens.BorderThin, color = MaterialTheme.colorScheme.outline),
    )
}
