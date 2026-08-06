# ADR-0007: Security Rules e id de vínculo determinístico

- **Status:** Aceito
- **Data:** 2026-08-06
- **Item do backlog:** E0-06

## Contexto

O E0-06 exige Security Rules com testes no emulador, e a regra central está escrita no próprio
item: *"Treinador só lê alunos com `link.status == active`"*.

Regras do Firestore **não podem fazer consulta** — só `get()` e `exists()` por caminho exato. O
§3.1 do backlog define `links/{linkId}` sem dizer como o id é formado. Com id arbitrário, uma regra
avaliando `studentSummaries/{studentId}` não tem como descobrir se existe vínculo entre o
solicitante e aquele aluno: ela precisaria consultar `links` por dois campos, o que a linguagem não
permite.

## Decisão

**O id do vínculo é determinístico: `links/{trainerId}_{studentId}`.** Deixa de ser detalhe de
implementação e passa a ser contrato — as regras dependem dele.

As regras negam por padrão (`match /{document=**}` recusando tudo ao final) e declaram
explicitamente cada coleção do §3.1.

Testes em `firestore/rules.test.js`, rodando contra o emulador com
`@firebase/rules-unit-testing`, executados por `node --test` — o runner nativo do Node, sem
framework adicional. Job próprio no CI, em paralelo ao build Android.

Duas regras merecem destaque por serem produto, não segurança genérica:

- **`sessions`: o treinador pode alterar a prescrição, nunca o campo `exercises`.** É a exceção do
  E0-05 — "nunca sobrescrever registro do aluno" — aplicada no servidor, e não apenas no cliente.
- **`links` não aceita `delete`.** Encerrar é mudar `status` para `ended`; apagar destruiria o
  histórico que os dois lados têm direito de manter (§4, D9).

## Alternativas consideradas

**Id aleatório com `get()` por consulta.** Impossível: a linguagem de regras não consulta.

**Duplicar o vínculo dentro de cada documento** (campo `trainerId` em `studentSummaries`, por
exemplo) e checar só o campo, sem `get()`. Mais barato — zero leitura extra — mas o campo vira
mentira no instante em que o vínculo encerra, e a regra passaria a autorizar treinador já
desvinculado. Usado apenas onde o dado é do próprio titular (`sessions`), onde não há essa
defasagem.

**Regras permissivas agora, restritivas depois.** Rejeitado: com o banco em modo bloqueado, o custo
de escrever as regras certas agora é o mesmo, e "depois" costuma chegar com dado real dentro.

## Consequências

**Cada `get()`/`exists()` em regra conta como leitura de documento na cota** — o modelo de
capacidade de §2.3 não previa isso. Por esse motivo as verificações de vínculo ficaram apenas onde
são indispensáveis, e o caminho mais quente do produto — o aluno lendo e gravando o próprio treino
— não faz nenhuma: resolve comparando `request.auth.uid` com campo do próprio documento. Quando
houver medição real (SP-04), o custo das regras precisa entrar na conta.

O código que criar vínculos **precisa** usar o id no formato combinado. Um teste garante que id
fora da convenção é recusado, então a violação aparece como erro de permissão, não como bug
silencioso de autorização.

O emulador é um processo Java. Quem rodar os testes localmente precisa de `java` no PATH — o mesmo
tropeço que já apareceu no hook de pré-commit.

Ficaram **fora**: `threads`, `reviews` e `assessments`, coleções do §3.1 sem modelagem definida
ainda. Como o padrão é negar, elas estão bloqueadas até ganharem regra própria — o esquecimento
vira erro de permissão no desenvolvimento, não vazamento em produção.

## Quando revisitar

Em E3 (vitrine), que abre leitura pública de `trainerProfiles` e cria `reviews` — a superfície de
exposição muda de figura. E em E0-07 (App Check), que fecha a brecha de enumeração de
`inviteCodes`, hoje legível por qualquer autenticado que conheça o código.
