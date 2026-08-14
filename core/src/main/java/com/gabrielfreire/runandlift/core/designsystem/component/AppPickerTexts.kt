package com.gabrielfreire.runandlift.core.designsystem.component

import androidx.compose.runtime.Immutable

/**
 * As sete frases de uma tela de seleção, reunidas.
 *
 * Reunidas e não soltas na assinatura porque sete parâmetros de texto estourariam o limite de seis
 * do projeto sozinhos, e porque na chamada eles viram sete literais em sequência onde trocar duas
 * de lugar compila e passa despercebido.
 *
 * Vêm de fora pela regra do design system: `:core` não tem recursos de string e não decide idioma
 * (ver [AppPasswordField]). Quem tem `R.string` é o módulo de tela.
 *
 * @param title o que se está escolhendo, no topo da tela — "Estado", "Cidade".
 * @param searchLabel rótulo do campo de busca.
 * @param clearSearch descrição do botão que apaga a busca, para o leitor de tela.
 * @param empty o que dizer quando a busca não encontrou nada. Sem isso a tela fica em branco, e
 *   branco parece travamento.
 * @param failure o que dizer quando a lista não pôde ser carregada.
 * @param retry rótulo da nova tentativa. Recusa sem saída é beco: se a lista falhou, tem de haver
 *   um botão, e não só um aviso.
 * @param back descrição da seta de voltar.
 */
@Immutable
data class AppPickerTexts(
    val title: String,
    val searchLabel: String,
    val clearSearch: String,
    val empty: String,
    val failure: String,
    val retry: String,
    val back: String,
)
