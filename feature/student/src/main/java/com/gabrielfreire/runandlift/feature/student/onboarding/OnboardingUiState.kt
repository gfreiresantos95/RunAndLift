package com.gabrielfreire.runandlift.feature.student.onboarding

/**
 * Estado do onboarding do aluno.
 *
 * @param step o passo na tela.
 * @param position posição dele na sequência, a partir de 1 — é o "passo 2 de 4" do cabeçalho.
 * @param total quantos passos a sequência tem **agora**. Cresce de quatro para seis quando o aceite
 *   de dado de saúde é dado, porque aí passam a existir perguntas que antes não cabia fazer.
 * @param saving verdadeiro durante a gravação do último passo. É a única espera do fluxo: os
 *   anteriores só mudam estado em memória, e por isso não precisam de rede nem podem falhar.
 * @param failed a gravação final não completou. O fluxo **não** prende ninguém aqui — a tela
 *   oferece tentar de novo e seguir, e o que não foi gravado reaparece como aviso na home.
 * @param finished terminou, com ou sem respostas. Quem observa isto navega para a home.
 */
internal data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.LEVEL,
    val position: Int = 1,
    val total: Int = OnboardingStep.ALWAYS_SHOWN.size,
    val saving: Boolean = false,
    val failed: Boolean = false,
    val finished: Boolean = false,
) {

    /** Se é o último passo da sequência — muda o rótulo do botão de "Continuar" para "Concluir". */
    val isLast: Boolean get() = position == total
}
