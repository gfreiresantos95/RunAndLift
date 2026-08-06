# ADR-0003: Estrutura de módulos e injeção de dependência

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E0-01

## Contexto

O E0-01 pede `:app :core :data :feature-*`, MVVM e `minSdk 26`. O projeto estava em um módulo só,
com o design system em `app/ui/theme` e a regra de abertura da splash dentro da `MainActivity`.

Duas perguntas não estavam respondidas no backlog: quantos módulos criar de imediato, e qual
mecanismo de injeção usar — a tabela de stack (§2.1) não menciona DI em lugar nenhum.

## Decisão

**Três módulos:** `:app`, `:core` e `:data`. `:feature-*` é padrão, não módulo, e nasce com a
primeira tela (E1-02). A dependência anda em um sentido só: `:app → :core`, `:app → :data`,
`:data → :core`. `:core` não depende de ninguém.

O design system saiu de `app/ui/theme` para `:core`, no pacote
`com.gabrielfreire.runandlift.core.designsystem`. `:core` expõe Compose e Material 3 por `api`,
para que módulo de tela não precise redeclarar as mesmas dependências.

**Injeção manual** por um `AppContainer` criado na `RunAndLiftApplication`.

`minSdk` subiu de 24 para 26.

## Alternativas consideradas

**Criar `:feature-onboarding` vazio junto.** Rejeitado: módulo sem conteúdo cobra configuração de
build e sincronização da IDE sem entregar separação nenhuma. O padrão de feature se demonstra na
primeira tela real, não em um esqueleto.

**Convention plugins em `build-logic`.** Rejeitado *por ora*. Com três módulos a duplicação é de
~20 linhas por arquivo, ainda legível. O AGP 9 mudou a DSL (`compileSdk { version = release(37) }`),
o que acrescenta risco a plugins de convenção agora. **Gatilho para adotar:** o segundo módulo
`:feature-*`, quando passarem a ser cinco arquivos de build repetindo a mesma configuração.

**Hilt ou Koin.** Rejeitado por ora. Hoje não existe repositório, banco nem cliente de rede: o
grafo nasceria vazio, e o Hilt cobraria processamento de anotação em todo build desde já.
**Gatilho:** E0-03 (repositórios com Room) ou E0-04, onde o `HiltWorker` do WorkManager passa a
valer o custo. Migrar depois é mecânico — anotar ViewModels e construtores.

**Manter `minSdk 24`.** Rejeitado: 26 é decisão registrada no backlog (§2.5). Custo aceito: sai o
Android 7.0 e 7.1.

## Consequências

A `MainActivity` deixou de guardar estado: a decisão de sair da splash virou `MainViewModel`, com
`isReady` exposto como `StateFlow`. Esse é o formato que os próximos ViewModels seguem — estado
somente-leitura para fora, mutação restrita, zero referência a `Context` ou tipo de UI.

O `AppContainer` está **sem membros de propósito**. Ele é a costura, não o grafo: existe para que a
primeira dependência não precise inventar um caminho. Se continuar vazio quando E0-03 entregar, é
sinal de que alguém injetou por outro lugar.

`app/lint.xml` virou `core/lint.xml`, seguindo o design system, e `:core` também recebeu
`lintChecks(compose-lint-checks)` — sem isso as regras de Compose deixariam de rodar justamente
sobre o módulo que concentra o Compose.

O `minSdk 26` tornou o qualificador `mipmap-anydpi-v26` redundante; a pasta virou `mipmap-anydpi`.

## Quando revisitar

A estrutura, quando o segundo `:feature-*` existir — aí entram convention plugins, e vale reavaliar
se `:core` deve virar `:core:designsystem` + `:core:common`. A injeção, em E0-03/E0-04, conforme o
gatilho acima.
