# ADR-0016: Um módulo por papel e navegação por abas

- **Status:** Aceito
- **Data:** 2026-08-13
- **Itens do backlog:** E0-08, E2-06, E6-01
- **Revisita:** [ADR-0009](0009-navegacao-por-papel-e-fronteira-de-feature.md), cujo gatilho era
  exatamente este — "quando o segundo `:feature-*` existir".

## Contexto

O ADR-0009 deixou os grafos de treinador e de aluno como duas telas de espera dentro do `:app`, e
registrou que a estrutura seria revisitada quando o segundo módulo de feature nascesse. Chegou a
hora: as duas telas de espera dão lugar a três abas por papel — início, treinos e menu.

A pergunta é onde essas telas moram. Elas não são do fluxo de entrada, então `:feature-auth` está
fora; deixá-las no `:app` era a terceira opção, e é o que existia.

## Decisão

**Dois módulos, `:feature-student` e `:feature-trainer`.** Um por papel, e não um `:feature-home`
com dois pacotes dentro.

A razão é a mesma que o ADR-0009 usou para recusar o grafo único com condicionais, aplicada um
nível acima: com dois módulos, o código do aluno **não enxerga** o do treinador. Não há como
importar uma rota do outro papel por engano, porque ela não está no classpath. O que era disciplina
de nomenclatura vira erro de compilação.

**A moldura das abas mora no `:core`.** `AppTabScaffold`, `AppBottomBar` e `AppBottomBarItem` são
desenho sem domínio: barra superior com título, conteúdo, barra inferior. Os dois módulos as
consomem passando os próprios textos, como todo componente do design system (`:core` não tem
`strings.xml`, e não decide idioma).

**As três abas são rotas irmãs dentro do grafo do papel**, e não um `NavHost` aninhado por aba. Aba
não é fluxo. Uma pilha por aba resolveria um problema que não existe enquanto nenhuma tem tela
filha, e cobraria por isso um grafo dentro do outro.

**O alternador de papel saiu do topo de toda tela raiz e foi para o menu.** Continua aparecendo só
para quem tem os dois papéis — a regra do ADR-0009 não mudou, mudou o lugar.

## Alternativas consideradas

**Um `:feature-home` com pacotes `student/` e `trainer/`.** Menos arquivos de build e nenhuma
duplicação de esqueleto. Rejeitado porque devolveria a separação entre papéis ao terreno da
disciplina, que é justamente o que o ADR-0009 recusou: dentro de um módulo, nada impede a tela do
aluno de importar a rota do treinador, e o erro só apareceria em produção, na forma de uma tela que
um papel não deveria alcançar.

**Continuar no `:app`.** O caminho mais curto, e o que existia. Rejeitado pelo mesmo motivo, somado
a um segundo: o `:app` viraria o lugar onde toda tela nasce, e a fronteira de feature deixaria de
existir na prática.

**Compartilhar as três telas entre os papéis, parametrizando o conteúdo.** As duas homes são hoje
quase idênticas — barra superior e card de identidade. Rejeitado porque a semelhança é temporária:
a home do aluno recebe o treino do dia e o aviso de cadastro incompleto, a do treinador recebe a
carteira de alunos. Um componente parametrizado para os dois teria vida curta e terminaria em
condicionais por papel, que é o desenho que o projeto recusa desde o ADR-0009.

**Convention plugins agora.** O gatilho registrado no ADR-0003 disparou junto: `feature-student` e
`feature-trainer` têm `build.gradle.kts` praticamente idênticos ao do `:feature-auth`. Adiado de
propósito para não misturar mudança de build com entrega de tela — três arquivos repetidos ainda
cabem na cabeça, e o custo de extraí-los não cresce por esperar mais um módulo.

## Consequências

`:app` deixou de conhecer o nome de qualquer tela dos papéis. `RoleRoutes` encolheu para uma função
que traduz papel em grafo, e as rotas passaram a ser declaradas por quem as desenha.

Os módulos duplicam o `MainDispatcherRule` e os fakes de repositório nos seus source sets de teste,
porque source set de teste não se compartilha entre módulos. Trinta linhas repetidas custam menos,
por ora, que um módulo `:test-fixtures` com a configuração de build que ele exige. **Gatilho:** o
terceiro módulo que precisar dos mesmos fakes.

O nome do app agora existe como string em três módulos — `:app`, `:feature-student` e
`:feature-trainer` —, porque feature não enxerga o aplicativo. É o preço da seta de dependência
apontar para um lado só, e foi pago conscientemente.

Duas decisões de interface ficaram embutidas e merecem registro:

- **O rótulo da aba fica sempre visível**, inclusive na inativa. O padrão do Material esconde o
  texto das não selecionadas, deixando a barra dependendo de ícone e realce de cor — dois canais que
  falham juntos para quem enxerga cor de forma diferente (E0-09).
- **Sair da conta usa botão de contorno, não preenchido.** A ação mais destacada de uma tela deve
  ser a que se espera que a pessoa faça, e ninguém abre o aplicativo para sair dele.

## Quando revisitar

Quando o treinador ganhar a carteira de alunos (E2-06) e o aluno o treino do dia (E6-01): é aí que
se saberá se três abas bastam ou se o treinador precisa de uma quarta. Como os módulos são
separados, essa resposta pode ser diferente para cada papel sem que um mexa no outro — o que é
metade do motivo desta decisão.
