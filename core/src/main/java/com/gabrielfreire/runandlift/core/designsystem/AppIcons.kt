package com.gabrielfreire.runandlift.core.designsystem

import androidx.annotation.DrawableRes
import com.gabrielfreire.runandlift.core.R

/**
 * Ícones do design system, pelo id do recurso.
 *
 * Existe para que uma tela não precise importar o `R` do `:core` só para desenhar um ícone. O `R`
 * de outro módulo funciona, mas é um detalhe de empacotamento vazando para o código de tela: no dia
 * em que um ícone mudar de módulo, quem tem `core.R.drawable.ic_home` escrito quebra, e quem tem
 * [AppIcons.Home] não percebe.
 *
 * São ids (`Int`), e não `Painter`: `painterResource` é `@Composable` e obrigaria a barra inferior a
 * montar os ícones dentro da composição, o que impede descrever uma aba fora dela — que é
 * exatamente o que [com.gabrielfreire.runandlift.core.designsystem.component.AppBottomBarItem] faz.
 *
 * Todos são vetores locais de 24dp. O desenho tem 24dp; a **área de toque** de quem os usa continua
 * sendo os 48dp de [Dimens.MinTouchTarget], que é responsabilidade do componente e não do ícone.
 */
object AppIcons {

    @DrawableRes
    val Home: Int = R.drawable.ic_home

    @DrawableRes
    val Workouts: Int = R.drawable.ic_workouts

    @DrawableRes
    val Menu: Int = R.drawable.ic_menu

    @DrawableRes
    val Logout: Int = R.drawable.ic_logout

    @DrawableRes
    val Back: Int = R.drawable.ic_arrow_back

    @DrawableRes
    val Search: Int = R.drawable.ic_search

    @DrawableRes
    val Clear: Int = R.drawable.ic_close

    /** Marca o campo que abre uma escolha, em vez de aceitar digitação. */
    @DrawableRes
    val Dropdown: Int = R.drawable.ic_arrow_drop_down

    /** Confirmação. Acompanha a cor onde "marcado" precisa de um segundo canal além dela. */
    @DrawableRes
    val Check: Int = R.drawable.ic_check
}
