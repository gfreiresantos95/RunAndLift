package com.gabrielfreire.runandlift.feature.trainer.menu

/**
 * O que o menu do treinador pode fazer.
 *
 * Reunidas porque a tela passou a ter dois destinos, o alternador de papel e a saída — quatro
 * callbacks soltos estouram o limite de parâmetros da função, e cada item novo do menu obrigaria a
 * mexer na assinatura dela e na de quem a chama.
 *
 * O alternador fica **fora** desta classe de propósito: ele é opcional (`null` some com o botão), e
 * misturá-lo aqui faria um campo desta classe significar "às vezes existe".
 */
internal data class TrainerMenuActions(
    val onOpenAccount: () -> Unit,
    val onOpenProfile: () -> Unit,
    val onSignOut: () -> Unit,
)

/** As ações sem efeito, para os previews. */
internal fun previewTrainerMenuActions() = TrainerMenuActions(
    onOpenAccount = {},
    onOpenProfile = {},
    onSignOut = {},
)
