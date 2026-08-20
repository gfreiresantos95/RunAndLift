package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus

// Carteira de mentira para desenhar o painel da home no Android Studio.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Isto existe
// para desenhar tela e não roda em produção — ao contrário de `TrainerDashboard.SAMPLE`, que é o
// exemplo que a home mostra de verdade enquanto não há treino registrado, e que por isso tem teste.

/**
 * Uma carteira com os quatro estados representados.
 *
 * É de propósito que ela tenha pedido pendente: é o estado em que a peça de pedidos ganha cor, e o
 * único em que o preview mostra alguma coisa que uma carteira só de ativos esconderia.
 */
internal fun previewRoster() = TrainerRoster(
    links = listOf(
        previewHomeLink(LinkStatus.ACTIVE, name = "Bruno Lima"),
        previewHomeLink(LinkStatus.ACTIVE, name = "Carla Nogueira"),
        previewHomeLink(LinkStatus.ACTIVE, name = "Rafael Moreira"),
        previewHomeLink(LinkStatus.REQUESTED, name = "Ana Souza"),
        previewHomeLink(LinkStatus.PAUSED, name = "Juliana Castro"),
        previewHomeLink(LinkStatus.ENDED, name = "Diego Martins"),
    ),
)

private fun previewHomeLink(status: LinkStatus, name: String) = Link(
    trainerId = "treinador-1",
    studentId = "aluno-${name.hashCode()}",
    status = status,
    origin = LinkOrigin.INVITE_CODE,
    trainerName = "Carlos Pereira",
    studentName = name,
)
