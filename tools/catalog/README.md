# tools/catalog

Constrói e publica o **catálogo global de exercícios** na coleção `exercises` do Firestore.

Não é um módulo do Gradle e não entra no aplicativo. É a ferramenta que existe porque a Security
Rule diz, em código e em comentário, que *"o cliente nunca escreve no catálogo global — ele é
publicado por fora do app"*.

## Por que fora do app

`firestore.rules` libera `create` em `exercises` só quando `request.resource.data.ownerId` é o
próprio autor. Catálogo global é `ownerId: null`, então **nenhuma pessoa autenticada consegue
gravá-lo** — nem o dono do projeto pelo aplicativo. Quem grava é o Admin SDK, com chave de conta de
serviço, que passa por cima das regras.

O outro motivo é o orçamento de leitura (§2.4, regra 5): o catálogo é o maior conjunto de dados do
produto, é versionado, e só é baixado quando `exercise_catalog_version` sobe no Remote Config.

## A origem

[`joao-gugel/exercicios-bd-ptbr`](https://github.com/joao-gugel/exercicios-bd-ptbr), variante
`exercises-ptbr-full-translation.json` — 873 exercícios com **nome, instruções e metadados em
português do Brasil**, tradução idiomática (`Lying Leg Curls` → `Cadeira Flexora`, não "Flexão de
Perna Deitado"). É derivação 1:1 de
[`yuhonas/free-exercise-db`](https://github.com/yuhonas/free-exercise-db), que está sob **The
Unlicense** (domínio público).

As duas variantes da base (`partial` e `full`) trazem **nome e instruções byte a byte idênticos** —
conferido nos 868 exercícios. A diferença é só a língua dos *slugs* de metadado. A `full` foi
escolhida por manter tudo em português desde a origem.

Dois detalhes que o nome "full translation" esconde, e que o `vocabulary.js` resolve:

- **Os metadados em português são slugs, não texto de tela**: `peso-do-corpo`,
  `inferior-das-costas`, `avancado` — sem acento e com hífen. Continuam precisando de mapeamento;
  o que muda é a direção dele, que passa a ser slug → texto legível em vez de inglês → português.
- **`force` segue em inglês** (`pull`, `push`, `static`) nas duas variantes, apesar de o README da
  base afirmar o contrário.

Se um dia for preciso voltar ao projeto pai em domínio público, o que muda é este arquivo de
vocabulário — as tabelas passam a partir do inglês. O resto do importador não sabe a diferença.

> **Pendência conhecida:** o repositório de origem **não tem arquivo de licença** (`license: null`
> na API do GitHub), embora o autor o anuncie como open source. Sem licença expressa, vale "todos os
> direitos reservados", e uma tradução é obra protegida por conta própria mesmo quando o original é
> domínio público. Já foi pedido um `LICENSE` ao autor. Enquanto não vier, o caminho seguro é tratar
> o arquivo como **referência de terminologia** e ter o texto revisado e reescrito do nosso lado —
> que é trabalho que a revisão profissional exige de qualquer forma.

## Como rodar

```bash
cd tools/catalog
npm install

# 1. Transforma a base no formato da coleção `exercises`
node build-catalog.js /caminho/para/exercises-ptbr-full-translation.json

# 2. Confere sem gravar nada
node import-catalog.js --dry-run

# 3. Publica
GOOGLE_APPLICATION_CREDENTIALS=/caminho/chave-conta-de-servico.json \
  node import-catalog.js --project <id-do-projeto>
```

A chave sai do Console do Firebase → Configurações do projeto → Contas de serviço → Gerar nova
chave. **Ela nunca entra no Git** — o `.gitignore` cobre `*serviceAccount*.json` e o diretório
inteiro de saída.

Depois de publicar, **suba `exercise_catalog_version` no Remote Config**. Sem isso nenhum aparelho
baixa o catálogo novo: é esse número que `OfflineFirstExerciseRepository.syncIfOutdated()` compara
com o que está no aparelho, e é ele que faz a sincronização custar zero leitura quando não há
novidade.

## O que é versionado e o que não é

| Arquivo | No Git? | Por quê |
|---|---|---|
| `vocabulary.js`, `build-catalog.js`, `import-catalog.js` | **sim** | São a decisão: o que se traduz, o que vira identificador, o que se recusa |
| `catalog.json` | não | Dado gerado. 900 KB que um comando reproduz em dois segundos |
| A base de origem | não | Baixada do repositório de terceiro |
| Chave de conta de serviço | **nunca** | Acesso administrativo ao projeto inteiro |

## O vocabulário

`vocabulary.js` tem duas políticas, e a diferença é o ponto:

- **Músculo e equipamento viram texto em português com acento.** No modelo de domínio são
  `List<String>` e `String?` livres, e é sobre eles que `ExerciseDao.search` roda o `LIKE`. Guardar
  `inferior-das-costas` faria a busca por "lombar" não encontrar nada — e mostraria um slug na tela.
- **Nível, mecânica, força e categoria viram identificadores** (`BEGINNER`, `COMPOUND`, `PULL`,
  `STRENGTH`), porque são conjuntos fechados que viram `enum` no Kotlin — a convenção do projeto,
  a mesma de `TrainingLevel` e `InjuryArea`: `:data` guarda o identificador, a feature traduz com
  `R.string`.

Valor desconhecido **derruba a importação inteira**, de propósito. Um músculo novo na base que
caísse aqui como `undefined` viraria exercício sem grupo muscular no catálogo, e ninguém perceberia
até um treinador procurar por ele e não achar.
