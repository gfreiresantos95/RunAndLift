package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.annotation.DrawableRes

/**
 * Uma aba da barra inferior.
 *
 * É um dado, e não um `@Composable`, porque a lista de abas precisa existir **antes** da composição
 * — quem monta a tela decide qual está selecionada comparando rota, e isso é lógica de navegação,
 * não desenho.
 *
 * @param label o texto abaixo do ícone. Obrigatório, e não opcional: a barra deste app nunca
 *   esconde o rótulo. Ícone sozinho é adivinhação, e "Menu" com três linhas seria confundido com
 *   uma gaveta lateral que não existe. É também a regra do projeto de a cor — aqui, o realce da aba
 *   ativa — nunca ser o único canal de informação (E0-09).
 * @param icon id de um vetor do design system, tipicamente de
 *   [com.gabrielfreire.runandlift.core.designsystem.AppIcons].
 * @param selected se esta é a aba em que o usuário está.
 * @param onClick o que fazer ao tocar. Tocar na aba já selecionada deve ser inofensivo: quem
 *   constrói decide se reempilha ou ignora.
 */
data class AppBottomBarItem(
    val label: String,
    @param:DrawableRes val icon: Int,
    val selected: Boolean,
    val onClick: () -> Unit,
)
