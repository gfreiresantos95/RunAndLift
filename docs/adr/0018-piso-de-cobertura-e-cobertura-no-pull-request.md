# ADR-0018: Piso de cobertura e cobertura medida no Pull Request

- **Status:** Aceito
- **Data:** 2026-08-15

## Contexto

O ADR-0006 decidiu **Kover só com relatório, sem piso**. Duas coisas mudaram desde então.

A primeira é que o relatório estava medindo quase nada. A agregação declarava um módulo só —
`kover(project(":data"))` — e desde então nasceram `:feature:auth`, `:feature:student` e
`:feature:trainer`. Dos 44 arquivos de teste do repositório, **37 estavam fora da conta**. O número
que o `koverHtmlReport` mostrava não era um recorte deliberado: era uma agregação que parou de
acompanhar o projeto.

A segunda é que o repositório passou a ter PRs com revisão obrigatória (ADR-0015), e nenhum deles
diz se o código que está entrando tem teste. Descobrir isso lendo o diff é justamente o trabalho que
o CI deveria fazer.

**A medição, feita em 2026-08-15 com a agregação corrigida**, deu:

| Denominador | Linhas | Branches |
|---|---|---|
| Tudo que compila | 46,1% (1006/2183) | 57,7% |
| Depois dos filtros abaixo | **63,4%** (1006/1587) | 61,1% |

Por módulo, em linhas: `feature:student` 84,8%, `feature:trainer` 76,9%, `feature:auth` 75,5%,
`app` 61,0%, `data` 36,0%, `core` 27,4%.

**258 das 581 linhas descobertas — 44% de toda a lacuna — são os adaptadores Firebase do `:data`**:
`FirestoreUserRepository` (79), `FirestoreStudentRepository` (61), `FirebaseAuthRepository` (50),
`IbgeLocationRemoteDataSource` (26), `FirestoreExerciseRemoteDataSource` (20) e `DataContainer`
(22). É consequência direta da estratégia do ADR-0006: os dublês são escritos na interface, então a
implementação Firestore nunca é exercitada por teste de JVM.

## Decisão

**A agregação cobre os seis módulos**, e o relatório exclui quatro categorias:

1. **`@Composable` e `ComposableSingletons`** — o projeto não tem teste de UI por decisão; a tela se
   confere pelo `@Preview`. Sem esta exclusão, cada tela entra como zero e o percentual mede uma
   decisão documentada em vez de uma lacuna.
2. **Código gerado pelo Room (`*_Impl`)** — 300 linhas de DAO e banco escritas pelo KSP. Testá-las é
   testar o Room; o que é nosso na persistência se verifica por teste de migração.
3. **`*PreviewFixtures*`** — dados de exemplo dos `@Preview`, que não rodam em produção.
4. **Tokens do design system** (`ColorKt`, `ColorSchemeKt`, `ExtendedColorSchemeKt`, `TypeKt`,
   `ShapeKt`, `Dimens`, `AppMotion`, `AppIcons`) — 220 linhas de constante declarativa, sem ramo nem
   decisão. O teste possível repetiria o literal do fonte; a verificação real é a galeria
   `ThemePreviews` em light e dark.

Os adaptadores Firebase **não** são excluídos: ali dentro mora a trava de consentimento de dados de
saúde, que é regra de LGPD e hoje não tem um único teste. É lacuna real, não ruído de medição.

**Dois pisos, em lugares diferentes porque respondem a perguntas diferentes:**

- **Projeto ≥ 60%, em linhas**, como regra do Kover (`koverVerify`), valendo no CI e na máquina de
  quem for abrir o PR.
- **Diff ≥ 80%**, medido só sobre as linhas que o PR mudou, publicado como comentário no PR e
  reprovando o job `verify` quando não atinge.

**Os 75% que motivaram esta discussão viram meta declarada, não piso.** Cobrindo `data.user`,
`data.student` e `data.auth` — as 190 linhas dos três repositórios Firestore — o total vai a 75,4%.
O piso sobe para 75 quando esse trabalho existir, e não antes.

**A action é a `madrapps/jacoco-report`, fixada por SHA** (`dc464cf1`), e não pela tag `v1.8.0`.

## Alternativas consideradas

**Manter o ADR-0006 como está, sem piso.** O argumento original continua de pé em parte: piso
premia teste de getter. Mas ele foi escrito quando o único módulo com teste era o `:data` e não
havia PR com revisão. Com quatro módulos e um fluxo de PR, a pergunta "isto que estou mergeando tem
teste?" passou a ter dono, e não é o revisor humano lendo diff.

**Piso do projeto em 75% já.** Rejeitado pela aritmética: a medição deu 63,4%, então o `main`
ficaria vermelho no primeiro push e a única saída seria escrever teste de repositório Firestore
antes de qualquer outra coisa. O piso passaria a pautar o roadmap. Um piso que se alcança e se sobe
vale mais que um que se suprime na primeira semana.

**Piso sobre branches em vez de linhas.** Branches (61,1%) medem melhor a qualidade do teste, já que
linha coberta sem o ramo alternativo é cobertura de fachada. Rejeitado por ora: são duas métricas
para explicar e negociar, e linha é a que a action publica no PR. Entra quando o piso chegar a 75.

**`madrapps/jacoco-report` na v1.8.0**, o último release estável (junho/2026). Rejeitado depois de
ler o código da action: na v1.8.0 o input chama-se `min-coverage-changed-files` e cobra a cobertura
do **arquivo inteiro** que foi tocado — a cobertura só das linhas novas aparece no comentário como
"delta", mas é informativa. Com o `:data` em 36%, isso significa que um PR que muda uma linha do
`FirestoreUserRepository` seria reprovado até aquele arquivo chegar a 80%: a regra cobraria do PR
uma dívida que não é dele. No `main` da action o input foi renomeado para
`min-coverage-changed-lines` e a saída para `coverage-changed-lines` — "the coverage of lines that
were changed in the PR" —, que é a semântica que esta decisão quer. Daí o pin por SHA: `dist/index.js`
está commitado naquela revisão, e SHA é pin imutável, ao contrário de uma tag móvel.

**Codecov.** Tem patch coverage maduro e lançado, histórico, gráfico de tendência e badge, e é
gratuito para repositório público. Rejeitado: exige `CODECOV_TOKEN` nos secrets, manda o relatório
de cobertura para fora do GitHub, e os checks `codecov/patch` e `codecov/project` só bloqueariam
depois de o ruleset do ADR-0015 passar a exigi-los pelo nome. É a alternativa certa se o pin por SHA
der problema, ou no dia em que a tendência ao longo do tempo passar a importar mais que o número do
PR.

**SonarQube Cloud.** Faria cobertura e quality gate de código novo numa tacada. Rejeitado por
sobreposição: o projeto já tem detekt, Android Lint e compose-lint-checks, e dois analisadores
estáticos com opiniões diferentes sobre o mesmo código é atrito sem ganho.

**Excluir também os adaptadores Firebase do denominador**, como fronteira com SDK externo. Rejeitado:
levaria o número para perto de 80% sem uma linha de teste nova, e apagaria da vista justamente a
`FirestoreStudentRepository`, onde vive a trava de consentimento de saúde. Um denominador que
esconde a regra de LGPD não está medindo qualidade, está maquiando.

## Consequências

O `koverVerify` roda no CI e localmente, então o piso é descoberto antes do push. O job continua se
chamando `verify` — o ruleset do ADR-0015 exige os checks pelo nome, e renomear job trava PR.

O comentário de cobertura é atualizado no lugar de duplicar (`update-comment: true` com `title`), e
o passo declara `continue-on-error: false` **explicitamente**: o padrão da action é `true`, e com ele
o piso viraria enfeite — comentaria o número e deixaria o merge passar.

O passo do relatório é separado do passo de testes para que o XML exista mesmo quando a cobertura
reprova. Sem isso, o PR que mais precisa do comentário seria o que não recebe nenhum.

O comentário é pulado em PR vindo de fork, onde o `GITHUB_TOKEN` é somente leitura. Como o
repositório é de um autor só, a alternativa — falhar com erro de permissão em vez de por cobertura —
seria um check vermelho que não explica nada.

O `:core` fica em 27,4% e não vai subir muito: o que sobrou nele depois dos filtros é
`AppMaskedTextField` e pouco mais. É o módulo que mais puxa o total para baixo sem que isso signifique
risco, porque o resto dele é layout.

Rodar a suíte no CI ficou mais caro: o `koverXmlReport` instrumenta e roda os testes das variantes
debug e release dos seis módulos.

## Quando revisitar

**Sobe para 75%** quando `FirestoreUserRepository`, `FirestoreStudentRepository` e
`FirebaseAuthRepository` tiverem teste — são as 190 linhas que faltam para chegar lá, e o teste da
trava de consentimento de saúde é o mais urgente deles.

**Troca o SHA pela tag** quando a v2.0 da action sair.

**Volta a discussão para o Codecov** se o pin por SHA quebrar duas vezes, ou quando a pergunta
deixar de ser "este PR tem teste?" e passar a ser "a cobertura está subindo ou caindo ao longo dos
meses?" — tendência é o que uma action sem estado não consegue responder.
