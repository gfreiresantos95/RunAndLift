# ADR-0017: Estados de tela, movimento e largura de conteúdo

- **Status:** Aceito
- **Data:** 2026-08-14
- **Itens do backlog:** E0-09, E6-01
- **Guia derivado:** [docs/design-guidelines.md](../design-guidelines.md)

## Contexto

O design system do projeto nasceu bem cuidado na parte estática — escala tipográfica com dígitos
tabulares, papéis de cor do Material 3 completos, contraste AA conferido, alvo de toque de 48 dp,
rótulo sempre visível na barra inferior. Uma auditoria da interface inteira contra as diretrizes do
Material 3 e as orientações do Google não achou nada de errado nessa base.

O que ela achou foi outra coisa: **o app tinha desenho, e não tinha comportamento**. As lacunas
eram todas do mesmo tipo — o que a tela faz enquanto espera, quando não tem nada, quando falha,
quando a pessoa sai de uma para a outra, e quando ela abre num aparelho que não é um telefone.

O levantamento, por ordem de gravidade:

1. **Carregar era desenhar nada.** Três telas faziam `if (loading) return`, o que deixa a barra
   superior sobre uma área em branco. Branco não se lê como "carregando", se lê como "quebrado".
2. **Salvar não confirmava.** A tela fechava sozinha, sem mensagem. Não havia como distinguir
   "salvou" de "voltou sem salvar" — e é o oposto da razão de a tela existir, que é corrigir um dado
   e ver a correção pegar.
3. **A navegação não tinha movimento.** O `NavHost` usava o padrão do Compose Navigation: um
   esmaecimento de 700 ms, lento o bastante para ser percebido como travamento e — o que é pior —
   **sem direção**. Ir e voltar eram o mesmo movimento.
4. **Vazio era um parágrafo no canto.** Alinhado ao topo e à esquerda, que é onde o olho espera o
   começo de uma lista. Lê-se como conteúdo que não carregou.
5. **A barra superior não reagia à rolagem.** Transparente por decisão, ela deixava o texto do
   formulário deslizar por trás do título, e as duas camadas se misturavam.
6. **A barra inferior usava `surface`.** Neste esquema `surface` e `background` têm o mesmo valor,
   então a barra ficava exatamente da cor do conteúdo — sem separação e sem chão.
7. **Erro era um `Text` vermelho solto.** Dependia inteiramente da cor, competia com as linhas de
   apoio dos campos, e não era anunciado: quem usa TalkBack tocava em "Salvar" e não recebia nada.
8. **Nada limitava a largura.** Em tablet, dobrável ou janela do ChromeOS, o formulário esticava de
   ponta a ponta e a linha passava de setenta e cinco caracteres.
9. **Nenhum retorno tátil.** Num aplicativo usado em pé, com a mão suada e o olho fora da tela, o
   toque não era confirmado por canal nenhum.

## Decisão

**Os estados de tela viram componentes do `:core`, e nenhuma tela os desenha à mão.**

| Estado | Componente | Por que assim |
| --- | --- | --- |
| Carregando | `AppLoadingState` | Indicador centralizado que **só aparece depois de 500 ms** |
| Vazio | `AppEmptyState` | Ícone em círculo, título, descrição e ação opcional, centralizados |
| Falha | `AppMessageCard` | Bloco com ícone, cor do papel `critical` e `liveRegion` |
| Confirmação | `AppSnackbarHost` | Some sozinho, porque um acerto não deve virar tarefa |

A espera antes do indicador é a parte que quase sempre falta e a que mais importa aqui: os
documentos vêm do cache do Firestore na maioria das aberturas e a carga termina em dezenas de
milissegundos. Um indicador imediato apareceria e sumiria num piscar — impressão de instabilidade,
pior do que não ter mostrado nada.

**Falha é bloco fixo; confirmação é snackbar.** A divisão não é estética. O snackbar some sozinho, e
sumir é certo para "salvo" e errado para uma falha que a pessoa precisa resolver — ela sumiria
justamente enquanto se relê o formulário procurando o que corrigir.

**Salvar confirma e permanece na tela.** Trocamos o fechamento automático por um aviso de
confirmação com a tela aberta. A seta de voltar continua no topo para quem terminou.

**A navegação usa eixo compartilhado horizontal**, com as durações e curvas do Material 3 reunidas
em `AppMotion`. A tela nova entra pela direita, a anterior sai pela esquerda, e o inverso ao voltar.
O deslocamento é um décimo da largura, e não a largura inteira: o eixo compartilhado sugere
movimento, não virada de página.

**Toda coluna de conteúdo passa por `AppScreenColumn`**, que limita a largura a 600 dp e centraliza.
No telefone não muda nada; é o que impede o app de ser um telefone esticado em tela grande.

**A barra superior ganha fundo ao rolar** (`pinnedScrollBehavior`), encapsulado nos scaffolds do
`:core` para nenhuma tela precisar tocar na API experimental do Material. É `pinned` e não
`enterAlways`: a barra carrega a seta de voltar, e uma saída que some ao rolar obriga a rolar de
volta para sair. A barra inferior passa a usar `surfaceContainer`, que é o papel que o Material 3
reserva para superfícies de navegação.

**Retorno tátil apenas em seleção que muda estado** — nunca em navegação. Vibrar a cada tela
transforma o retorno em ruído, e ruído constante é indistinguível de nenhum retorno.

## Consequências

Ganhamos um vocabulário de estados que toda tela nova herda de graça, e o custo é que ela **precisa
usá-lo**: uma tela que desenhe o próprio "carregando" volta a ter o problema que este ADR fecha. O
guia derivado existe para isso.

Duas coisas ficaram de fora, conscientemente:

- **Esqueletos de conteúdo** no lugar do indicador circular. São melhores quando a forma do conteúdo
  é previsível — a lista de treinos será o caso —, e são desperdício num formulário cujos campos
  mudam de altura conforme o consentimento. Revisitar quando a aba de treinos tiver conteúdo.
- **Transição de elemento compartilhado** entre a lista de alunos e o detalhe. Não há lista ainda.

## Alternativas descartadas

**Manter o fechamento automático ao salvar e mostrar a confirmação na tela anterior.** É o padrão
mais comum, e exigiria carregar o aviso através da fronteira de navegação até uma aba que pertence a
outro módulo. Custo alto de encanamento para um resultado pior: a confirmação apareceria numa tela
onde o dado corrigido nem está visível.

**`enterAlwaysScrollBehavior` na barra superior**, que esconde a barra ao rolar e devolve espaço.
Boa para lista longa de leitura, ruim aqui: as telas que mais rolam são formulários, e a barra
carrega a única saída deles.

**Cor dinâmica para modernizar a paleta.** Já descartada no [ADR-0003] e reafirmada: o semáforo de
aderência precisa significar a mesma coisa no aparelho de todo mundo.

[ADR-0003]: 0003-estrutura-de-modulos-e-injecao-de-dependencia.md
