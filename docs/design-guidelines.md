# Diretrizes de interface

O que seguir ao escrever uma tela nova. A razão de cada regra está no KDoc do componente
correspondente e, quando é decisão de arquitetura, no [ADR-0017](adr/0017-estados-de-tela-movimento-e-largura-de-conteudo.md).

Este documento é a lista de conferência; ele não repete a argumentação.

## A regra que resume as outras

**Se você está desenhando uma moldura, um estado ou um espaçamento à mão, provavelmente existe um
componente para isso.** O `:core` tem a moldura de tela, a coluna de conteúdo, os quatro estados e a
grade de espaçamento. Tela que os refaz é tela que vai divergir do resto na primeira mudança.

## Toda tela tem quatro estados

Nenhum deles é opcional, e desenhar só o terceiro é o erro mais comum.

| Estado | O que usar |
| --- | --- |
| Carregando | `AppLoadingState` |
| Vazio | `AppEmptyState` |
| Com conteúdo | o conteúdo |
| Falhou | `AppMessageCard` |

- **Nunca** `if (loading) return`. Isso desenha uma tela em branco, que se lê como defeito.
- Vazio diz **o que vai aparecer** e **o que falta acontecer**, não "nada por aqui". Título no
  presente ("Seus treinos aparecem aqui"), descrição com o próximo passo.
- Vazio só ganha botão quando há ação possível **a partir daquela tela**. Botão inerte é pior que
  nenhum botão.
- Falha fica na tela até deixar de ser verdade. Confirmação é snackbar e some sozinha.

## Moldura e largura

- Tela de aba → `AppTabScaffold`.
- Tela com título e seta de voltar → `AppScreenScaffold`.
- Conteúdo rolável → `AppScreenColumn`, sempre. É ele que aplica a largura máxima de 600 dp.
- Nunca escrever `Scaffold` + `Column` + `verticalScroll` à mão numa tela. A ordem dos modificadores
  tem uma armadilha (recuo dentro da área rolável) que o componente já resolveu.
- Aplicar sempre o `PaddingValues` que o `Scaffold` entrega. Sem ele o conteúdo nasce atrás da barra
  inferior.

## Movimento

- Durações e curvas vêm de `AppMotion`. Não escrever `tween(300)` numa tela.
- Transição de navegação é responsabilidade do `NavHost` do `:app`; uma tela não anima a própria
  entrada.
- Dentro de uma tela, o que aparece e some usa `AnimatedVisibility` com `AppMotion.DURATION_SHORT`.
- Movimento que a pessoa não pediu não existe. Nada pisca, nada pulsa, nada chama atenção sozinho.

## Cor

- Consumir `MaterialTheme.colorScheme` e `MaterialTheme.extendedColors`. **Nunca** os tokens de
  `Color.kt` diretamente.
- Superfície de navegação usa `surfaceContainer`; card sobre o fundo usa `surfaceVariant`; realce de
  seleção usa `secondaryContainer`.
- **Cor nunca é o único canal.** Todo uso de cor com significado leva ícone ou rótulo junto —
  semáforo de aderência, chip marcado, mensagem de erro. É requisito (E0-09), não refinamento.
- `error` é falha de sistema ou validação; `critical` do tema estendido é estado legítimo do aluno.
  Não trocar um pelo outro.

## Tipografia

- Papéis do Material via `MaterialTheme.typography`. Número medido — carga, repetição, RPE,
  aderência — usa `MetricTextStyles`, que tem dígitos tabulares.
- Tudo em `sp`. Nada de `dp` em texto: a escala de fonte do sistema precisa funcionar.
- 14sp é o piso de conteúdo. 12sp só para rótulo de apoio.

## Alvo de toque e retorno

- Piso de 48 dp, sempre. O desenho pode ser menor; a área de toque não. `Modifier.minimumTouchTarget()`
  quando o componente não garantir sozinho.
- Retorno tátil (`rememberSelectionHaptics`) **só em seleção que muda estado**. Nunca em navegação,
  nunca em rolagem.

## Acessibilidade

- Ícone sozinho sempre tem `contentDescription`. Ícone ao lado de um rótulo que já diz a mesma coisa
  tem `contentDescription = null` — descrever os dois faz o leitor anunciar duas vezes.
- Mensagem que aparece sem o foco ir até ela precisa de `liveRegion`. É o caso de erro de envio e de
  indicador de carregamento.
- Controle composto — campo mais camada de toque, rótulo mais estado — usa
  `semantics(mergeDescendants = true)` para virar um nó só.
- Escolha entre irmãs usa `Role.RadioButton`; escolha independente usa `Role.Checkbox`. Não é
  detalhe: é o que faz o TalkBack anunciar "1 de 3" em vez de "botão".

## UX writing

O que este projeto já pratica, escrito para não se perder:

- **Segunda pessoa, e frase de gente.** "Qual é o seu objetivo?", não "Objetivo".
- **A linha de apoio diz para que serve a resposta**, antes de a pessoa errar. "Ajusta as faixas de
  esforço do seu treino" vale mais que uma mensagem de erro depois.
- **Erro diz o que fazer, não o que aconteceu.** "Escolha o seu estado", não "campo inválido". E
  aponta a parte errada: "Confira o número, a categoria e a sigla" em vez de "registro inválido".
- **O que o app não faz também se diz.** Minimização de dados e campo travado ganham a explicação
  junto — a pergunta que um campo cinza levanta é a que vira mensagem de suporte.
- **Recibo, não comemoração.** "Alterações salvas." A pessoa corrigiu um telefone, não bateu um
  recorde.
- **Nada de "ops", "ihh" ou exclamação em erro.** Quem está sem internet na academia não quer
  simpatia, quer o próximo passo.
- Texto em português; código, identificadores e nomes de arquivo em inglês.

## Previews

- Todo arquivo com layout tem `@Preview`. Componente usa `@LightDarkPreviews`; tela declara o par
  com `heightDp` próprio.
- O preview mostra o **estado que costuma sair errado**, e não o estado feliz: o campo travado, a
  lista vazia, o envio com todos os erros, o card sem monograma.
- Texto de preview vem de `stringResource` nos módulos que têm `strings.xml`, e de `PreviewSamples`
  no `:core`, que não tem por decisão.

## O que está fora, e por quê

- **Cor dinâmica (Material You)** — o semáforo de aderência precisa significar a mesma coisa em todo
  aparelho. Ver ADR-0003.
- **Esqueleto de conteúdo** — melhor que indicador circular quando a forma do conteúdo é previsível.
  Revisitar quando a aba de treinos tiver lista.
- **Transição de elemento compartilhado** — revisitar quando existir lista de alunos e detalhe.
- **Preferência de tema dentro do app** — item de Fase 2 (E0-12). Quando vier, alimenta o parâmetro
  `darkTheme` do tema, e não um tema paralelo.
