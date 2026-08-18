# ADR-0022: O piso do diff passa a reprovar de fato, e fiação sai do denominador

- **Status:** Aceito
- **Data:** 2026-08-17

## Contexto

O ADR-0018 criou o piso de 80% sobre as linhas mudadas e registrou, sobre a action que o publica:

> `continue-on-error: false` é obrigatório: o padrão da action é `true`, e com ele o piso viraria
> enfeite — comentaria o número e deixaria o merge passar.

**A afirmação está errada, e o piso era enfeite desde o primeiro dia.** O PR do vínculo
([ADR-0020](0020-vinculo-por-codigo-de-convite.md)) foi o primeiro a ficar abaixo do limiar, e o
resultado foi um comentário marcando `Changed lines 78.93% ❌` com o job `verify` **verde**.

A leitura do fonte da revisão fixada (`madrapps/jacoco-report@dc464cf1`, `src/action.ts`) explica: os
únicos `core.setFailed` são para input faltando ou inválido e para exceção capturada, e
`continue-on-error` governa exatamente esse `catch`. Os dois limiares — `min-coverage-overall` e
`min-coverage-changed-lines` — só escolhem entre o emoji de aprovado e o de reprovado na tabela do
comentário. A action **publica** o número; ela nunca reprovou nada.

Medir também estava errado por outro motivo. O recorte que a action usa são todas as linhas mudadas
que aparecem no relatório, e no PR do vínculo isso incluía:

| O que puxava o número para baixo | Linhas |
|---|---|
| `StudentGraph` e `TrainerGraph` — registro de rota | ~24 |
| `MyTrainerActions`, `StudentsActions`, `InviteActions`, `StudentMenuActions` — holders de callback | ~13 |
| `DataContainer`, `AppContainer`, `*Dependencies` — fiação de injeção | ~5 |

Nenhuma dessas linhas tem ramo, decisão ou regra. São listas de "isto liga naquilo", e o que as
verifica é abrir o app — do mesmo jeito que o que verifica um token de cor é a galeria de previews.

## Decisão

**O piso do diff passa a reprovar num passo próprio do CI**, que lê a saída
`coverage-changed-lines` da action e falha quando ela fica abaixo de 80. A action continua fazendo o
que sabe fazer — comentar — e a régua deixa de depender de um comportamento que ela não tem.

O passo só roda quando o de comentário rodou (`steps.cobertura.outcome == 'success'`), e trata saída
vazia como "não há diff mensurável", não como reprovação: PR de fork não comenta, e push em `main`
não tem PR. A comparação é em `awk` porque o shell só compara inteiro e a cobertura tem decimal.

**Fiação declarativa sai do denominador**, pelo mesmo critério que já tirou os tokens do design
system: sem ramo, sem decisão, e verificável olhando o app e não uma linha coberta.

- `*.navigation.*GraphKt` — registro de rotas.
- `*.navigation.*Dependencies`, `*.di.AppContainer`, `DataContainer` — fiação de injeção.
- `*.*Actions` — `data class` que só reúne callbacks.

**O que continua no denominador**, e a fronteira importa: `*Routes`, porque montar rota é string com
regra dentro (`TrainerRoutesTest` existe justamente por isso), e todo `*ViewModel`, `*UiState` e
validação — que é onde a decisão mora.

**O piso do projeto continua em 60 e a meta continua em 75.** O número saltou de 69,5% para 74,8%
com esta exclusão, e isso **não** é a meta alcançada: as 190 linhas dos três repositórios Firestore
continuam sem um único teste, e o `FirestoreStudentRepository` continua guardando a trava de
consentimento de saúde sem nada afirmando que ela funciona. Declarar a meta cumprida por mudança de
denominador seria a maquiagem que o ADR-0018 recusou, e o alvo de 75% passa a valer **medido com os
adaptadores do vínculo de volta no denominador** — hoje 72,0% (1607/2232), contra 74,8% (1607/2149)
no relatório que o `koverVerify` lê.

## Alternativas consideradas

**Deixar informativo e corrigir só a documentação.** É o que o repositório já tinha na prática, e
tem defesa: o comentário no PR informa, e o revisor decide. Rejeitado porque o repositório é de um
autor só e a revisão é do próprio autor — o comentário que ninguém é obrigado a olhar é exatamente o
tipo de sinal que se aprende a rolar para baixo. Se a régua não vale, é melhor não ter régua do que
ter uma que todos acreditam existir.

**Tornar o piso real sem excluir a fiação.** Rejeitado pela consequência imediata: o PR do vínculo
reprovaria em 78,93%, e o desbloqueio seria escrever teste para registro de rota — que é
precisamente o que o projeto decidiu não fazer ao dispensar teste de UI. Um piso que se atinge
escrevendo o teste que a arquitetura dispensa é um piso que ensina a burlar.

**Trocar a action por uma que reprove sozinha, ou pelo Codecov.** Rejeitado por ora, pela mesma razão
do ADR-0018: o Codecov leva o relatório para fora do GitHub e exige secret. Um passo de dez linhas
lendo uma saída resolve o problema inteiro, e continua funcionando quando a v2.0 da action sair.

**Baixar o piso do diff para 70%, calibrando pela realidade.** Rejeitado: o número que estava
atrapalhando não era o piso, era o denominador. Corrigido o denominador, 80% é atingível pelo código
que de fato decide alguma coisa.

## Consequências

O PR do vínculo passa a medir acima do piso pelo motivo certo: o que sobrou no denominador é
ViewModel, estado, mapeamento e regra, e tudo isso tem teste.

**A partir daqui, um PR abaixo de 80% no diff não entra na `main`** sem bypass de administrador — o
check `verify` fica vermelho de verdade. É a primeira vez que isso é verdade desde o ADR-0018.

O comentário da action continua sendo a explicação: ele lista arquivo por arquivo e aponta as linhas
descobertas. O passo novo só decide o desfecho, e a mensagem de erro cita o número e o ADR.

A série histórica de cobertura ganha um degrau artificial em 2026-08-17 (69,5% → 74,8%), pela
segunda vez no mesmo dia. Os números da data ficam registrados para a comparação continuar possível:
**67,1%** com tudo dentro, **69,5%** só com os adaptadores do vínculo fora, **72,0%** só com a fiação
fora, **74,8%** com os dois — este último é o que o `koverVerify` passa a ler.

## Quando revisitar

**O passo de piso sai** se a action passar a reprovar sozinha — provável na v2.0, que também é
quando o SHA vira tag. O comentário do passo diz o que conferir.

**A exclusão de fiação é reaberta** no dia em que houver teste de navegação (por exemplo,
`TestNavHostController` afirmando que cada rota resolve para um destino). Aí grafo volta ao
denominador, porque passa a ser verificável por teste.
