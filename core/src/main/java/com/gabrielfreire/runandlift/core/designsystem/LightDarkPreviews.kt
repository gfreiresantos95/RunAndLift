package com.gabrielfreire.runandlift.core.designsystem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * O mesmo composable nos dois temas, numa anotação só.
 *
 * Existe porque a regra do projeto é conferir **sempre** claro e escuro: os dois esquemas são
 * espelhados (tom 40 no claro ↔ tom 80 no escuro) e é justamente aí que uma troca de token quebra
 * o contraste de um par `on<Papel>` / `<Papel>` sem ninguém perceber. Anotar um preview só é como
 * metade dos componentes acabaria sem nunca ter sido vista no escuro.
 *
 * Serve a **componente**, que se inspeciona no tamanho que tem. Tela longa continua declarando o
 * par de `@Preview` na mão, porque precisa de `heightDp` próprio — valor que uma multipreview não
 * consegue variar por uso.
 */
@Preview(name = "Claro", showBackground = true)
@Preview(name = "Escuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class LightDarkPreviews
