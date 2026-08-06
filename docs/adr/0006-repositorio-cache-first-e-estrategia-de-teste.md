# ADR-0006: Repositório cache-first e estratégia de teste

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E0-03

## Contexto

O E0-03 pede "Room como fonte de verdade local + repositórios com estratégia cache-first", e é
marcado como base de D8 (travamento e perda de dados) e do offline real. É item G — grande demais
para uma entrega só.

Duas pressões o moldam. A primeira é o ambiente de uso: academia em subsolo, sem rede, e o treino
precisa abrir do mesmo jeito. A segunda é o orçamento de leitura (§2.4): a leitura do Firestore é o
recurso escasso, e o catálogo de exercícios é o maior conjunto de dados do produto.

## Decisão

**Fatia entregue:** infraestrutura completa, provada por uma vertical real — o catálogo de
exercícios. As demais entidades chegam com suas telas. O catálogo foi escolhido por não depender
de autenticação nem de vínculo, e por exercer diretamente a regra 5 do orçamento de leitura.

**Contrato cache-first**, que os próximos repositórios devem seguir:

1. Leitura nunca vai à rede. Todo `observe*` sai do Room.
2. Sincronizar é operação explícita; o resultado chega pela `Flow` do banco, não pelo retorno.
3. Falha de rede é valor de retorno, não exceção.
4. Download do catálogo só quando a versão remota for maior que a local.

**Versão do catálogo vem do Remote Config**, através da interface `CatalogVersionSource` definida
em `:data` e implementada em `:app`.

**Testes com dublês escritos à mão**, sem biblioteca de mock. **Kover só com relatório**, sem piso
de cobertura.

## Alternativas consideradas

**Modelar as 12 coleções do §3.1 agora.** Rejeitado: fixaria esquema para telas que não existem, e
a migração versionada (E0-13) ainda não está pronta — corrigir sairia caro.

**Provar a infraestrutura com `sessions` em vez do catálogo.** É o coração do D8 e seria mais
fiel ao espírito do item. Rejeitado porque `sessions` depende de `assignments`, `programs` e
`users`: arrastaria modelagem especulativa de entidades cujas telas ainda não existem.

**Consultar a versão do catálogo em um documento do Firestore.** Seria autocontido em `:data`, sem
inversão de dependência. Rejeitado: custaria 1 leitura por sincronização, e a regra 5 existe para
levar esse custo a zero. O Remote Config é gratuito e ilimitado.

**MockK para os dublês.** Estava aprovado e disponível. Não usado: as interfaces têm poucos métodos
e o que os testes afirmam é **estado** — o que ficou no banco, quantas vezes a rede foi tocada. Um
fake com estado diz isso de forma mais direta que uma cadeia de `every { } returns`, e não quebra
quando a assinatura muda de forma irrelevante. **Gatilho para adotar:** a primeira colaboração em
que o que importa seja a *interação* (ordem, número de chamadas com argumentos específicos) e não
o estado resultante.

**Piso de cobertura no Kover.** Rejeitado: percentual mínimo nesta fase premiaria teste de getter e
não diria nada sobre a política, que é o que realmente sustenta o offline.

**`fallbackToDestructiveMigration`.** Rejeitado explicitamente no código: apagar o banco do usuário
para resolver mudança de esquema é exatamente a perda de dados descrita em D8.

## Consequências

O `AppContainer` deixou de estar vazio: monta o `DataContainer` e expõe o `ExerciseRepository`. A
costura de injeção manual agora tem uso real, que era a condição registrada no ADR-0003.

`AppDispatchers` vive em `:data` porque só `:data` consome. Quando `:app` precisar, isso força a
partição de `:core` prevista no ADR-0003.

O esquema do Room é exportado para `data/schemas/1.json`, versionado. Sem ele, E0-13 não teria de
onde migrar.

Duas coisas ficaram **não testadas**: o DAO contra banco real, que exige `androidTest` ou
Robolectric, e a integração com o Firestore, que exige o emulador (E0-06). Os testes atuais cobrem
política, não mecânica — é uma cobertura deliberadamente parcial, não um descuido.

O `ReturnCount` do detekt apontou três saídas em `sync()`. Em vez de afrouxar a regra, a decisão de
cota virou uma função com nome, `isDownloadWorthwhile` — o que a deixou explícita e testável.

## Quando revisitar

Quando a segunda entidade entrar (E5/E6). Se o `OfflineFirstExerciseRepository` virar molde copiado
e colado, o padrão comum deve virar abstração — mas só aí, com dois casos reais na mão, e não antes.
