package com.gabrielfreire.runandlift.feature.trainer.home

/**
 * Estado da home do treinador.
 *
 * @param loading verdadeiro até a identidade ser lida. Como no aluno, a tela não fica em branco
 *   esperando: o card aparece sem o nome e o ganha quando ele chega.
 * @param displayName nome gravado no cadastro, ou `null` para quem entrou pelo Google e ainda não
 *   completou o perfil.
 */
internal data class TrainerHomeUiState(val loading: Boolean = true, val displayName: String? = null) {

    /** Primeira letra do primeiro nome, em maiúscula, ou `null` quando não há nome. */
    val monogram: String? get() = displayName?.trim()?.firstOrNull()?.uppercase()
}
