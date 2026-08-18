package com.gabrielfreire.runandlift.feature.trainer.invite

// As ações da tela de convite, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura.

/** Gerar, compartilhar, tentar de novo e voltar — os quatro sem fazer nada. */
internal fun previewInviteActions() = InviteActions(
    onGenerate = {},
    onShare = {},
    onRetry = {},
    onBack = {},
)
