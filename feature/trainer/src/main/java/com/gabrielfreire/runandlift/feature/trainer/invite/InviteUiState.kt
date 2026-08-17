package com.gabrielfreire.runandlift.feature.trainer.invite

/**
 * O código de convite do treinador.
 *
 * `null` em [code] é "ainda não gerou nenhum", e não "não consegui ler" — quem responde a segunda
 * pergunta é [failed]. A diferença decide o que a tela oferece: gerar o primeiro código, ou tentar
 * ler de novo.
 *
 * @param working uma gravação em curso. Gerar código é a única ação da tela, e ela precisa desabilitar
 *   o botão enquanto acontece: dois toques seguidos criariam dois códigos, e o segundo apagaria o
 *   primeiro logo depois de ele ter sido enviado a alguém.
 */
internal data class InviteUiState(
    val loading: Boolean = true,
    val working: Boolean = false,
    val failed: Boolean = false,
    val code: String? = null,
)
