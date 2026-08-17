package com.gabrielfreire.runandlift.feature.trainer.professionalform

// Cenários prontos do formulário profissional, para os previews.
//
// Ficam num arquivo próprio pela mesma razão do `ProfileFormPreviewFixtures` do `:feature:auth`:
// os previews que precisam deles estão espalhados por três pacotes — professionalform, onboarding
// e profile —, e sete lambdas vazias repetidas em cada um é o tipo de ruído que faz a pessoa parar
// de escrever preview.
//
// O nome do arquivo não é decoração: `*PreviewFixtures*` é o que o kover exclui da cobertura. Este
// código existe para desenhar tela no Android Studio e **não roda em produção** — medi-lo seria
// medir a decisão documentada de não ter teste de UI, e não uma lacuna de teste.

/** As ações sem efeito, para os previews — a tela se desenha, e nada acontece ao tocar. */
internal fun previewTrainerFormActions() = TrainerFormActions(
    onExperienceSelect = {},
    onSpecialtyToggle = {},
    onServiceModeToggle = {},
    onDayToggle = {},
    onBioChange = {},
    onMaxStudentsChange = {},
    onShowcaseChange = {},
)
