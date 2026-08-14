package com.gabrielfreire.runandlift.feature.auth.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Colhe o que a tela de seleção deixou para trás e entrega ao formulário.
 *
 * É a outra metade do "abrir para obter um resultado": [LocationPickerDestination] escreve no
 * `SavedStateHandle` desta entrada antes de se desempilhar, e este efeito reage à escrita.
 *
 * **Limpa a chave depois de usar**, e isso não é higiene: sem limpar, o valor continuaria lá, e
 * qualquer recomposição futura — voltar de outra tela, girar o aparelho — reaplicaria a mesma
 * escolha por cima de uma escolha mais nova.
 *
 * Estado e cidade têm efeitos separados porque as duas escolhas chegam em momentos diferentes e uma
 * delas mexe na outra: trocar o estado invalida a cidade, e um efeito só teria de decidir a ordem
 * das duas coisas a cada disparo.
 *
 * @param onStatePicked recebe sigla e nome. As duas, porque o formulário grava uma e desenha a outra.
 */
@Composable
internal fun PickedLocationEffect(
    handle: SavedStateHandle,
    onStatePicked: (String, String) -> Unit,
    onCityPicked: (String) -> Unit,
) {
    val uf by handle.getStateFlow<String?>(LocationPickerResult.STATE_UF, null)
        .collectAsStateWithLifecycle()
    val city by handle.getStateFlow<String?>(LocationPickerResult.CITY, null)
        .collectAsStateWithLifecycle()

    LaunchedEffect(uf) {
        val picked = uf ?: return@LaunchedEffect
        // O nome é lido direto, e não observado: ele nunca chega sozinho — quem o escreve escreve a
        // sigla no mesmo instante, e é a sigla que dispara este efeito.
        val name = handle.get<String>(LocationPickerResult.STATE_NAME).orEmpty()

        handle[LocationPickerResult.STATE_UF] = null
        handle[LocationPickerResult.STATE_NAME] = null

        onStatePicked(picked, name)
    }

    LaunchedEffect(city) {
        val picked = city ?: return@LaunchedEffect

        handle[LocationPickerResult.CITY] = null

        onCityPicked(picked)
    }
}
