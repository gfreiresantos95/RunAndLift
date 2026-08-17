package com.gabrielfreire.runandlift.feature.trainer.account

// Os eventos da tela de dados cadastrais, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Isto existe
// para desenhar tela no Android Studio e não roda em produção.

/** Os sete eventos da tela, nenhum deles fazendo nada. */
internal fun previewAccountActions() = AccountActions(
    onNameChange = {},
    onPhoneChange = {},
    onOpenStatePicker = {},
    onOpenCityPicker = {},
    onSubmit = {},
    onSaved = {},
    onBack = {},
)
