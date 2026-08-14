package com.gabrielfreire.runandlift.feature.student.menu

/**
 * O que o menu do aluno pode fazer.
 *
 * Reunidas porque a tela passou a ter três destinos e o alternador de papel — quatro callbacks
 * soltos estouram o limite de parâmetros da função, e cada item novo do menu obrigaria a mexer na
 * assinatura dela e na de quem a chama.
 *
 * O alternador fica **fora** desta classe de propósito: ele é opcional (`null` some com o botão), e
 * misturá-lo aqui faria um campo desta classe significar "às vezes existe".
 */
internal data class StudentMenuActions(
    val onOpenAccount: () -> Unit,
    val onOpenTraining: () -> Unit,
    val onSignOut: () -> Unit,
)

/** As ações sem efeito, para os previews. */
internal fun previewStudentMenuActions() = StudentMenuActions(
    onOpenAccount = {},
    onOpenTraining = {},
    onSignOut = {},
)
