package com.gabrielfreire.runandlift.feature.trainer.programs

import com.gabrielfreire.runandlift.data.model.Program

/**
 * Estado da aba de treinos do treinador: os moldes que ele já montou.
 *
 * @param failed leitura que não respondeu. Separado de [isEmpty] pela mesma razão da carteira:
 *   "você ainda não montou nenhum programa" dito a quem tem doze é a pior frase que a tela pode
 *   produzir, e é exatamente o que sai quando falha de rede é desenhada como vazio.
 * @param deleting o id do programa cuja exclusão está em curso, para a linha não aceitar um segundo
 *   toque enquanto a primeira escrita não voltou.
 */
internal data class ProgramsUiState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val programs: List<Program> = emptyList(),
    val deleting: String? = null,
) {

    /** Sem programa nenhum — e sem falha, que é outra conversa. */
    val isEmpty: Boolean get() = programs.isEmpty()

    fun isDeleting(program: Program): Boolean = deleting == program.id
}
