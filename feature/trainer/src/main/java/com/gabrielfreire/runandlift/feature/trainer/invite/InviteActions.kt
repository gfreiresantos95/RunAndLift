package com.gabrielfreire.runandlift.feature.trainer.invite

/**
 * O que se faz na tela do convite.
 *
 * @param onShare entrega o código a outro aplicativo — é assim que ele chega ao aluno, e é por isso
 *   que **não existe um botão de copiar**: o código sai daqui para uma conversa, e copiar deixaria a
 *   pessoa procurando onde colar. Quem quiser ditá-lo tem o código na tela, em corpo grande.
 */
internal data class InviteActions(
    val onGenerate: () -> Unit,
    val onShare: (String) -> Unit,
    val onRetry: () -> Unit,
    val onBack: () -> Unit,
)
