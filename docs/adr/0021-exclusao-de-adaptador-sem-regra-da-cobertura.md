# ADR-0021: Adaptador sem regra sai do denominador da cobertura, um nome por vez

- **Status:** Aceito
- **Data:** 2026-08-17

## Contexto

O ADR-0018 criou dois pisos: projeto ≥ 60% e **diff ≥ 80%**, este último medido só sobre as linhas
que o PR muda, reprovando o job `verify`. Ele também rejeitou, explicitamente, excluir os
adaptadores Firebase do denominador — "levaria o número para perto de 80% sem uma linha de teste
nova, e apagaria da vista justamente a `FirestoreStudentRepository`, onde vive a trava de
consentimento de saúde".

O vínculo entre treinador e aluno ([ADR-0020](0020-vinculo-por-codigo-de-convite.md)) foi o primeiro
PR a encostar no limite dessa regra. A medição do código novo:

| Recorte | Linhas cobertas | Linhas descobertas | Cobertura |
|---|---|---|---|
| Total do PR | 224 | 103 | **68,5%** |
| Adaptador do SDK | 0 | 83 | 0% |
| Todo o resto | 224 | 20 | **91,8%** |

As 83 linhas são chamada de SDK do Firestore: `collection().whereEqualTo().limit().get().await()`,
composição de `WriteBatch`, leitura de `DocumentSnapshot`. Nenhuma delas roda em teste de JVM, pela
mesma estratégia do ADR-0006 — os dublês são escritos na interface, então a implementação Firestore
nunca é exercitada.

**Não é problema de esforço, é de aritmética.** Com 83 linhas impossíveis no numerador, chegar a 80%
exigiria 332 linhas cobertas no mesmo PR; havia 224, e o que dava para extrair do adaptador já tinha
sido extraído: o id determinístico e os mapas de gravação em `LinkDocument`, o alfabeto e a
normalização do código em `InviteCodeDocument`, e a decisão entre criar, reabrir e recusar em
`LinkRequest` — os três com teste. O piso, nesse formato, cobra de um PR de integração uma coisa que
ele não pode entregar, e o efeito prático seria ensinar a usar o bypass de administrador.

## Decisão

**Um adaptador entra na lista de exclusão do Kover quando não sobrou regra dentro dele**, e é
nomeado um a um em `build.gradle.kts`.

O critério é o que muda em relação ao ADR-0018, e ele é verificável no PR: para acrescentar um nome
à lista é preciso apontar **onde mora a regra que saiu daquela classe e qual teste a afirma**. Sem
esse par, o nome não entra. A exclusão vira o prêmio por extrair a lógica do adaptador, e não o
esconderijo dela.

Entram agora, com as regras que saíram de dentro deles:

| Classe excluída | Regra extraída | Teste |
|---|---|---|
| `FirestoreLinkRepository` | `LinkDocument` (id `{trainerId}_{studentId}`, mapas de gravação), `LinkRequest` (criar, reabrir ou recusar) | `LinkDocumentTest`, `LinkRequestTest` |
| `FirestoreInviteCodes` | `InviteCodeDocument` (alfabeto de seis caracteres, normalização do que foi digitado) | `InviteCodeDocumentTest` |
| `LinkSnapshotKt` | — leitura pura de `DocumentSnapshot`, sem decisão além de "campo faltando vira ausência" | — |

**Não entram, e a razão é a mesma do ADR-0018:**

- `FirestoreStudentRepository` — carrega a trava de consentimento de dados de saúde. Enquanto a regra
  estiver dentro dela, ela fica no denominador, medindo a lacuna real que ela é.
- `FirestoreUserRepository` — decide que o nome só é escrito quando não há nenhum, que é o que impede
  o cadastro de atropelar um nome editado.
- `FirebaseAuthRepository` — traduz exceção do SDK em `AuthFailure`, e a tradução é comportamento.
- `IbgeLocationRemoteDataSource` — analisa JSON à mão, o que é regra com ramo.

O piso do projeto **continua em 60** e a meta continua em 75. A exclusão mexe no denominador de 83
linhas hoje; o número do projeto sai de 67,1% para 69,5%, e isso não é motivo para subir o piso —
subir por exclusão seria exatamente o que o ADR-0018 chamou de maquiagem.

## Alternativas consideradas

**Bypass de administrador neste PR, mantendo a regra como está.** É o caminho mais curto e não mexe
em decisão nenhuma. Rejeitado porque não resolve: o próximo adaptador — `programs`, `assignments`,
`sessions` — encontra o mesmo muro, e a terceira vez que se contorna um piso ele deixa de ser piso.
Um gate que se ignora rotineiramente é pior que gate nenhum, porque consome atenção sem produzir
decisão.

**Baixar o piso do diff de 80% para 60%.** Rejeitado: o piso não está errado para o caso comum. A
maioria dos PRs deste repositório é ViewModel, validação e mapeamento, tudo testável, e 80% é
exatamente a régua certa para eles. Baixar por causa da exceção pune quem escreve o código que dá
para testar.

**Excluir a camada de adaptadores inteira**, por pacote (`*.data.*.Firestore*`). Rejeitado pelo
argumento original do ADR-0018, que continua correto: apagaria da vista a trava de consentimento de
saúde. A diferença desta decisão é ser uma lista, e não um padrão — cada nome é um ato deliberado,
revisável no diff, e o critério é objetivo.

**Infraestrutura de teste para o `:data`** — Robolectric ou emulador do Firestore, testando os
adaptadores de verdade. É a solução real, e continua sendo o alvo: destrava as 190 linhas que faltam
para a meta de 75% e tornaria esta exclusão desnecessária. Rejeitada **como pré-requisito deste PR**:
é um item de trabalho próprio, com decisão própria (Robolectric no `test` ou instrumentado no CI, e
o custo de cada um), e travar o vínculo até lá seria deixar o produto parado por uma métrica.

## Consequências

A cobertura do diff do PR do vínculo passa de 68,5% para acima de 90%, sem que uma linha de teste
tenha mudado de lugar — e é por isso que o critério de entrada na lista precisa ser cobrado na
revisão. **A pergunta do revisor deixa de ser "isto tem teste?" e passa a ser, para adaptador, "o que
saiu daqui, e onde está o teste do que saiu?"**

A lista cresce devagar por construção: são quatro nomes, e cada um custa uma extração e um teste
antes de existir.

O número do projeto sobe por mudança de denominador, o que polui a série histórica. A medição de
2026-08-17 fica registrada nos dois recortes — 67,1% com os adaptadores do vínculo, 69,5% sem — para
a comparação com agosto continuar possível.

O risco assumido é conhecido: **as 83 linhas excluídas continuam sem teste nenhum**. O que as protege
hoje são os 49 testes de Security Rules contra o emulador, que exercitam do lado do servidor
exatamente o que essas linhas escrevem, e o `@Preview` de cada tela. Não é a mesma coisa que testar o
adaptador, e este ADR não finge que é.

## Quando revisitar

**A lista é esvaziada** quando existir infraestrutura de teste para o `:data`. Nesse dia estas
classes voltam ao denominador e a meta de 75% vira piso.

**O critério é reaberto** se algum nome for acrescentado sem o par regra/teste, ou se alguém precisar
excluir um adaptador que ainda decide alguma coisa — é o sintoma de que a regra deveria ter saído
dele antes.
