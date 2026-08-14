package com.gabrielfreire.runandlift.core.designsystem.component

import com.gabrielfreire.runandlift.core.designsystem.PreviewSamples

// Montagens repetidas dos previews da tela de seleção.
//
// Em arquivo à parte, e não junto do componente, pela mesma razão dos `…PreviewFixtures` das telas
// de cadastro: são três previews que precisam dos mesmos sete textos e das mesmas quatro lambdas,
// e mantê-los no arquivo do componente o empurraria para além do limite de funções por arquivo —
// com o efeito colateral de misturar o que se desenha com o que se usa para desenhá-lo.

internal fun previewPickerTexts() = AppPickerTexts(
    title = PreviewSamples.Picker.TITLE,
    searchLabel = PreviewSamples.Picker.SEARCH,
    clearSearch = PreviewSamples.Picker.CLEAR,
    empty = PreviewSamples.Picker.EMPTY,
    failure = PreviewSamples.Picker.FAILURE,
    retry = PreviewSamples.Picker.RETRY,
    back = PreviewSamples.Action.BACK,
)

internal fun previewPickerActions() = AppPickerActions(
    onQueryChange = {},
    onSelect = {},
    onRetry = {},
    onBack = {},
)
