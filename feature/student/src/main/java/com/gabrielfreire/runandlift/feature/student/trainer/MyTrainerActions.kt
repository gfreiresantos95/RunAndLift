package com.gabrielfreire.runandlift.feature.student.trainer

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * O que se faz na tela do treinador do aluno.
 *
 * São seis porque a tela tem dois assuntos que se alternam — digitar um código e cuidar do vínculo
 * que existe —, e nenhum deles aparece ao mesmo tempo que o outro.
 *
 * @param onConfirmInvite pede o vínculo ao treinador que apareceu depois da busca. É o segundo dos
 *   dois passos, e o que de fato autoriza alguém a acompanhar o aluno.
 * @param onStatusChange aceitar um convite recebido ou encerrar o que existe. O aluno pode encerrar
 *   sozinho, sempre: um vínculo que só a outra parte desfaz não é um vínculo, é uma assinatura.
 */
internal data class MyTrainerActions(
    val onCodeChange: (String) -> Unit,
    val onSubmitCode: () -> Unit,
    val onConfirmInvite: () -> Unit,
    val onDismissInvite: () -> Unit,
    val onStatusChange: (Link, LinkStatus) -> Unit,
    val onBack: () -> Unit,
)
