package com.gabrielfreire.runandlift.feature.student.trainer

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkOrigin
import com.gabrielfreire.runandlift.data.model.LinkStatus

// Vínculos e ações de mentira para desenhar a tela no Android Studio.
//
// Arquivo próprio pelo nome: `*PreviewFixtures*` é o que o kover exclui da cobertura.

/** Um vínculo com nome dos dois lados, que é como ele chega de verdade. */
internal fun previewLink(status: LinkStatus, trainer: String = "Carlos Pereira") = Link(
    trainerId = "treinador-1",
    studentId = "aluno-1",
    status = status,
    origin = LinkOrigin.INVITE_CODE,
    trainerName = trainer,
    studentName = "Ana Souza",
)

/** As seis ações, sem efeito. */
internal fun previewMyTrainerActions() = MyTrainerActions(
    onCodeChange = {},
    onSubmitCode = {},
    onConfirmInvite = {},
    onDismissInvite = {},
    onStatusChange = { _, _ -> },
    onBack = {},
)

/**
 * Sem treinador e com um encerrado no histórico — o estado com mais coisa na tela, e o único em que
 * o campo de código aparece.
 */
internal fun previewNoTrainerState() = MyTrainerUiState(
    loading = false,
    links = listOf(previewLink(LinkStatus.ENDED, trainer = "Marina Alves")),
    code = "ABC234",
)
