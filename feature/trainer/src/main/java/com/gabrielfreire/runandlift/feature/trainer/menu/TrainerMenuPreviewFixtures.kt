package com.gabrielfreire.runandlift.feature.trainer.menu

// As ações do menu, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Isto existe
// para desenhar tela no Android Studio e não roda em produção.

/** Abrir as duas telas de perfil e sair da conta — os três sem fazer nada. */
internal fun previewTrainerMenuActions() = TrainerMenuActions(
    onOpenAccount = {},
    onOpenProfile = {},
    onSignOut = {},
)
