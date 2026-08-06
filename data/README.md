# :data

Camada de dados. **O Room é a fonte de verdade da UI**; o Firestore sincroniza para cá e nenhuma
tela o lê direto. É essa inversão que torna o offline real em vez de "cache que às vezes funciona".

## O que já existe (E0-03)

| Peça | Papel |
|---|---|
| `local/RunAndLiftDatabase` | Banco Room, esquema exportado em `data/schemas/` |
| `local/exercise/` | Entidade e DAO do catálogo de exercícios |
| `local/catalog/` | Versão da última sincronização do catálogo |
| `remote/exercise/` | Leitura do catálogo global no Firestore |
| `remote/catalog/CatalogVersionSource` | Contrato da versão publicada — implementado em `:app` sobre o Remote Config |
| `repository/ExerciseRepository` | Contrato cache-first |
| `repository/OfflineFirstExerciseRepository` | Implementação de referência |
| `DataContainer` | Porta de entrada: monta banco, fontes e repositórios |

## A pasta `schemas/`

Contém o esquema do banco exportado pelo Room, um arquivo por versão:

```
data/schemas/com.gabrielfreire.runandlift.data.local.RunAndLiftDatabase/1.json
```

**O nome é o número da versão do banco, e não pode ser trocado por algo descritivo.** O
`MigrationTestHelper` recebe um número de versão e procura exatamente `<versão>.json` neste
diretório; renomear faz o teste de migração falhar sem mensagem útil.

Trate a pasta como histórico, não como arquivos avulsos. Ao subir para `version = 2`, aparece um
`2.json` ao lado — e o `1.json` fica para sempre, porque é dele que a migração parte. Por isso os
arquivos são versionados no Git: sem eles, E0-13 não tem de onde migrar nem o que validar.

O diretório em si é configurável, no `ksp { arg("room.schemaLocation", ...) }` do
`data/build.gradle.kts`.

## Regras da fronteira

- **`:data` não depende de `:core` nem de `:app`.** Nenhum tipo do Room ou do Firestore atravessa
  para fora: entidades, DAOs e fontes de dados são `internal`. O que sai são interfaces e modelos
  de domínio (`model/`).
- **Leitura nunca vai à rede.** Todo `observe*` sai do Room e reemite sozinho quando a tabela muda.
- **Sincronizar é operação explícita**, e o resultado chega pela mesma `Flow` — não pelo retorno.
- **Falha de rede não é exceção**, é estado esperado: vira valor de retorno, e o app segue com o
  que tem em disco.
- **Toda ida à rede declara seu custo** em leituras do Firestore, no KDoc.

## O que ainda entra, e em qual item

| Conteúdo | Item |
|---|---|
| Entidades de treino: `sessions`, `programs`, `assignments`, `users`, `links` | E5, E6, E1, E2 |
| Fila de escrita offline com WorkManager e `clientWriteId` | E0-04 |
| Resolução de conflito por campo | E0-05 |
| Migração versionada de esquema | E0-13 |

## Testes

`OfflineFirstExerciseRepositoryTest` cobre a **política**, não a mecânica do Room: que ler não
gasta rede, que sincronizar só acontece quando a versão mudou, e que falha remota nunca destrói o
que está em disco. Usa dublês escritos à mão — as interfaces são pequenas e o que importa é estado.

O DAO em si só pode ser testado com banco de verdade, o que exige `androidTest` ou Robolectric.
Ainda não feito.
