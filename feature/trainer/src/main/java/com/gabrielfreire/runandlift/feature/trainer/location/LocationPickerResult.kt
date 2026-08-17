package com.gabrielfreire.runandlift.feature.trainer.location

/**
 * As chaves pelas quais a tela de seleção devolve a escolha para quem a abriu.
 *
 * É o "abrir para obter um resultado" da navegação do Compose: a tela de escolha escreve no
 * `SavedStateHandle` da entrada **anterior** da pilha e se desempilha; a tela de dados cadastrais,
 * ao voltar à composição, lê e limpa.
 *
 * **É uma terceira cópia do que o `:feature:auth` e o `:feature:student` têm**, e é deliberado,
 * pela mesma razão de
 * [com.gabrielfreire.runandlift.feature.trainer.validation.AccountFormValidation]: os módulos não
 * se enxergam. As chaves são as mesmas strings por coincidência de nome, não por contrato — cada
 * grafo entrega o resultado dentro de si, e nenhuma destas telas jamais lê o que a outra escreveu.
 *
 * O que **não** está duplicado é a parte cara: a busca sem acento vive em `:data`
 * ([com.gabrielfreire.runandlift.data.location.LocationSearch]) e o desenho da tela em `:core`.
 *
 * Este é o terceiro módulo a precisar disto, que era o gatilho declarado para extrair — e vale
 * dizer o que a terceira cópia mostrou: o que se repete são vinte linhas de cola de navegação, e o
 * único destino possível para elas seria um módulo compartilhado entre features, que é exatamente a
 * dependência que a arquitetura recusa. O gatilho continua de pé para a **régua** de validação, que
 * é regra de negócio; para estas chaves, ele mirava o lugar errado.
 */
internal object LocationPickerResult {

    /** Sigla do estado escolhido — o que vai ao banco. */
    const val STATE_UF = "location.state.uf"

    /** Nome por extenso do mesmo estado, para o campo poder exibir `São Paulo - SP`. */
    const val STATE_NAME = "location.state.name"

    /** Nome do município escolhido. */
    const val CITY = "location.city"
}
