package com.gabrielfreire.runandlift.feature.auth.location

/**
 * As chaves pelas quais a tela de seleção devolve a escolha para quem a abriu.
 *
 * É o "abrir para obter um resultado" da navegação do Compose: não existe `startActivityForResult`
 * aqui, e o que faz o papel dele é o `SavedStateHandle` da entrada **anterior** da pilha. A tela de
 * escolha escreve nele e se desempilha; o formulário, ao voltar à composição, lê e limpa.
 *
 * Constantes num objeto e não literais nos dois lados porque quem escreve e quem lê são arquivos
 * diferentes: uma letra trocada não quebraria a compilação, só faria a escolha sumir no caminho —
 * que é o defeito mais difícil de enxergar deste fluxo inteiro.
 *
 * O estado volta em **duas** chaves. Poderia ser uma só, com sigla e nome grudados, mas aí quem lê
 * precisaria desmontar a string — e a forma de desmontar é justamente o que este projeto não quer
 * escrito duas vezes.
 */
internal object LocationPickerResult {

    /** Sigla do estado escolhido — o que vai ao banco. */
    const val STATE_UF = "location.state.uf"

    /** Nome por extenso do mesmo estado, para o campo poder exibir `São Paulo - SP`. */
    const val STATE_NAME = "location.state.name"

    /** Nome do município escolhido. */
    const val CITY = "location.city"
}
