# ADR-0023: Drenagem de todos os adaptadores Firebase, e o preço do `isReturnDefaultValues`

- **Status:** Aceito
- **Data:** 2026-08-18

## Contexto

O [ADR-0021](0021-exclusao-de-adaptador-sem-regra-da-cobertura.md) criou o critério: um adaptador do
Firebase sai do denominador da cobertura **depois** que a regra que vivia dentro dele foi extraída
para um objeto comum e ganhou teste próprio. A exclusão é o prêmio por extrair, não o esconderijo.

Ele aplicou o critério a um adaptador só, o do vínculo, e deixou escrito o que faltava: "o resto da
camada de adaptadores Firebase continua no denominador, a `FirestoreStudentRepository` acima de
todos — ela ainda guarda a trava de consentimento de dado de saúde, que é onde está a maior lacuna
real". A medição de então era 63,4% no projeto.

Este ADR registra a aplicação do critério aos **seis** adaptadores restantes, e uma decisão de
ferramenta que ela exigiu.

## Decisão

### 1. Todo adaptador foi drenado, e cada regra extraída tem teste

| Adaptador | Regra extraída | Teste |
|---|---|---|
| `FirestoreStudentRepository` | `StudentDocument` — a trava do consentimento de saúde, os mapas de gravação parcial, lesão ausente ≠ lesão vazia | `StudentDocumentTest` |
| `FirestoreUserRepository` | `UserDocument` — acúmulo de papéis, nome só onde não há nenhum, nulo omite no cadastro e apaga na edição | `UserDocumentTest` |
| `FirestoreTrainerRepository` | `TrainerDocument` — a trava da vitrine (já existia) mais os três decodificadores de lista | `TrainerDocumentTest` |
| `FirebaseAuthRepository` | `AuthFailureMapping` — qual exceção do SDK vira qual motivo, **e em que ordem**; `ProviderName` — nome vazio é ausência de nome | `AuthFailureMappingTest`, `ProviderNameTest` |
| `FirestoreExerciseRemoteDataSource` | `ExerciseDocument` — exercício sem nome não entra, campo estranho não derruba a sincronização | `ExerciseDocumentTest` |
| `IbgeLocationRemoteDataSource` | `IbgePayload` — a ordenação em pt-BR e a sigla conferida antes de entrar na URL | `IbgePayloadTest` |

`LinkSnapshot` também foi drenado: ele estava na lista de exclusão do ADR-0021 **com a regra ainda
dentro** — documento incompleto vira ausência, origem desconhecida vira convite. A decisão passou
para `LinkDocument.link`, que recebe `String?` em vez de `DocumentSnapshot`, e ganhou teste.

O padrão que se repetiu nas seis: **o decodificador recebe `Any?` ou `String?`, e não o
`DocumentSnapshot`**. Ler campo por nome é chamada ao SDK; decidir o que fazer com o que veio é
regra. Separadas, a metade que decide é alcançável por um teste de JVM e a metade que chama o SDK é
o que a exclusão cobre.

Medição depois: **93,8%** no projeto (era 63,4%).

### 2. `unitTests.isReturnDefaultValues = true` no `:data`

A tradução de erro de autenticação só se afirma construindo as exceções do SDK, e o construtor de
`FirebaseException` chama `Preconditions.checkNotEmpty`, que chama `android.text.TextUtils.isEmpty`
— um método sem implementação no `android.jar` dos testes unitários, que o AGP faz lançar
`"not mocked"`.

Aceitamos ligar `isReturnDefaultValues` no `:data`, e o preço é declarado: método do Android não
mockado passa a devolver `null`/`0`/`false` **em silêncio** em vez de lançar. Um teste que encoste
sem querer numa API do Android recebe resposta errada sem aviso.

A alternativa era o Robolectric, que resolveria sem esse efeito e custaria uma dependência de teste
mais segundos por classe. Ficou de fora porque nenhum teste deste módulo precisa de comportamento do
Android — só de uma exceção construída. **O gatilho para trocar é o primeiro teste que precisar do
Android de verdade**, e não o próximo que precisar construir uma exceção.

O que sustenta essa aposta é a regra que o módulo já segue: os testes do `:data` são sobre regra em
Kotlin puro, e todo decodificador novo recebe tipo da linguagem, não do Firebase.

### 3. `authCall` captura `FirebaseAuthException` inteira

Antes, quatro `catch` de subclasse; agora um. É mudança de comportamento e vale dizê-la em voz alta:
uma exceção de autenticação que o app não mapeia — `FirebaseAuthRecentLoginRequiredException`, por
exemplo — **subia e derrubava a tela**, e agora vira `AuthFailure.UNKNOWN` e uma frase.

Duas razões. "Não foi possível entrar" é melhor do que o app fechar sozinho, e a exceção original
continua em `cause` para a telemetria de E0-11. E a **ordem** entre as subclasses passa a existir num
lugar só: `FirebaseAuthWeakPasswordException` **é** uma `FirebaseAuthInvalidCredentialsException`, e
com a ordem espalhada entre os `catch` e o `when` só o segundo teria teste.

## Consequências

- O piso de 60% do `koverVerify` deixou de dizer alguma coisa: com 93,8%, ele só reprovaria uma
  queda que ninguém comete por acidente. **Subi-lo fica pendente**, de propósito — o número novo
  deve sair de uma conversa sobre quanto se aceita perder, e não da medição de hoje virar exigência
  de amanhã por efeito colateral do PR que escreveu os testes.
- A lista de exclusão do ADR-0021 deixa de crescer por aqui: não há mais adaptador Firebase para
  drenar. O requisito para um nome novo continua o mesmo — apontar, no PR, onde a regra foi parar e
  qual teste a afirma.
- As fixtures de `@Preview` que escapavam do filtro pelo nome (`previewStudentMenuActions` e as
  outras duas) foram **movidas** para arquivos `*PreviewFixtures.kt`, em vez de o filtro ser
  alargado para `*ActionsKt`. Alargá-lo teria escondido junto a regra que fica em
  `TrainingFormActions.kt`: retirar o consentimento apaga peso, altura e lesões da memória.
