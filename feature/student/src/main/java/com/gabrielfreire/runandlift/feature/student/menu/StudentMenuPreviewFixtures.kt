package com.gabrielfreire.runandlift.feature.student.menu

// As ações do menu do aluno, sem efeito, para os previews.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Dentro de
// `StudentMenuActions.kt` esta função contava como lacuna de teste, e o que ela é de fato é código
// que existe para desenhar tela — a mesma natureza dos outros `*PreviewFixtures*` do módulo.

/** Conta, treino, treinador e sair — os quatro sem fazer nada. */
internal fun previewStudentMenuActions() = StudentMenuActions(
    onOpenAccount = {},
    onOpenTraining = {},
    onOpenTrainer = {},
    onSignOut = {},
)
