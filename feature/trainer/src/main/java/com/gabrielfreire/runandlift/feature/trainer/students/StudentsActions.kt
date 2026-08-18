package com.gabrielfreire.runandlift.feature.trainer.students

import com.gabrielfreire.runandlift.data.model.Link
import com.gabrielfreire.runandlift.data.model.LinkStatus

/**
 * O que se faz na carteira: convidar alguém, mudar o estado de um vínculo, e tentar de novo.
 *
 * Reunidas numa classe pela mesma razão das ações do menu: a tela recebe uma coisa em vez de três
 * parâmetros que crescem a cada botão novo.
 *
 * @param onStatusChange recebe o vínculo e o estado de destino. Aceitar, recusar, pausar, retomar e
 *   encerrar são a mesma ação com destinos diferentes — cinco funções aqui só repetiriam a máquina
 *   de estados que já mora nas Security Rules.
 * @param onRetry relê a carteira. Existe porque a falha de leitura tem de oferecer saída: um aviso
 *   que só informa deixa a pessoa fechando e reabrindo o app para conseguir o mesmo efeito.
 */
internal data class StudentsActions(
    val onOpenInvite: () -> Unit,
    val onStatusChange: (Link, LinkStatus) -> Unit,
    val onRetry: () -> Unit,
)
