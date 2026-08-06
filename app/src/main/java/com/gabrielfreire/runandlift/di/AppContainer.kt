package com.gabrielfreire.runandlift.di

/**
 * Grafo de dependências do aplicativo, criado uma vez em
 * [com.gabrielfreire.runandlift.RunAndLiftApplication] e vivo enquanto o processo existir.
 *
 * **Injeção manual, não Hilt nem Koin.** Hoje não há repositório, banco nem cliente de rede — um
 * framework de DI geraria um grafo vazio e cobraria processamento de anotação em todo build. O que
 * este arquivo entrega agora é a *costura*: o lugar onde as dependências passam a ser construídas,
 * para que a primeira delas não precise inventar um caminho.
 *
 * Como cresce: cada dependência vira uma propriedade `by lazy` aqui, e o ViewModel que precisa
 * dela ganha um parâmetro de construtor mais uma `viewModelFactory` que lê o container via
 * `APPLICATION_KEY`.
 *
 * Quando trocar por Hilt: quando o grafo tiver ramificação de verdade — escopo por papel
 * (treinador/aluno), `HiltWorker` para a fila de sincronização (E0-04), ou dependência que precise
 * viver menos que o processo. Manter DI manual além desse ponto é reescrever Hilt à mão, pior.
 *
 * A classe está sem membros de propósito. O primeiro a entrar deve ser o repositório de E0-03.
 */
class AppContainer
