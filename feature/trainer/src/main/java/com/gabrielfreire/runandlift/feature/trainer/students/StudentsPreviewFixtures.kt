package com.gabrielfreire.runandlift.feature.trainer.students

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus

// Vínculos de mentira para desenhar a carteira no Android Studio.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura. Isto existe
// para desenhar tela e não roda em produção.

/** Um vínculo com nome, para a linha aparecer como ela aparece de verdade. */
internal fun previewLink(status: LinkStatus, name: String = "Ana Souza") = Link(
    trainerId = "treinador-1",
    studentId = "aluno-${name.hashCode()}",
    status = status,
    origin = LinkOrigin.INVITE_CODE,
    trainerName = "Carlos Pereira",
    studentName = name,
)

/** As ações da carteira, sem efeito, para os previews. */
internal fun previewStudentsActions() = StudentsActions(
    onOpenInvite = {},
    onStatusChange = { _, _ -> },
    onRetry = {},
)

/**
 * A carteira no estado que mais revela problema de layout: um pedido no topo, dois alunos e um
 * encerrado. É onde se confere se os três blocos se distinguem sem legenda.
 */
internal fun previewStudentsState() = StudentsUiState(
    loading = false,
    links = listOf(
        previewLink(LinkStatus.ACTIVE, name = "Bruno Lima"),
        previewLink(LinkStatus.REQUESTED, name = "Ana Souza"),
        previewLink(LinkStatus.PAUSED, name = "Carla Nogueira"),
        previewLink(LinkStatus.ENDED, name = "Diego Martins"),
    ),
)
