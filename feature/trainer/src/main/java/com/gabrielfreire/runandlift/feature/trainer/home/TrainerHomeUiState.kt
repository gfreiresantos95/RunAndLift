package com.gabrielfreire.runandlift.feature.trainer.home

import com.gabrielfreire.runandlift.feature.trainer.profile.MissingTrainerData

/**
 * Estado da home do treinador.
 *
 * @param loading verdadeiro até a identidade ser lida. Como no aluno, a tela não fica em branco
 *   esperando: o card aparece sem o nome e o ganha quando ele chega.
 * @param displayName nome gravado no cadastro, ou `null` para quem entrou pelo Google e ainda não
 *   completou o perfil.
 * @param missing o que falta no perfil profissional. Enquanto a leitura não termina, é o vazio — e
 *   o aviso não aparece: um aviso que some sozinho um instante depois é pior que um aviso atrasado.
 */
internal data class TrainerHomeUiState(
    val loading: Boolean = true,
    val displayName: String? = null,
    val missing: MissingTrainerData = MissingTrainerData(),
) {

    /**
     * Uma letra para o círculo do card, ou `null` quando não há nome.
     *
     * A regra é a **primeira letra do primeiro nome**, em maiúscula. Nome composto não vira duas
     * letras de propósito: "Carlos Eduardo" com "CE" no círculo se confunde com sigla, e o card já
     * mostra o nome inteiro ao lado.
     */
    val monogram: String? get() = displayName?.trim()?.firstOrNull()?.uppercase()
}
