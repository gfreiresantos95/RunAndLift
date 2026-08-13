# Desenvolvimento

Documentação técnica do projeto: como montar o ambiente, o que já está escrito, como o código está
organizado e o que o CI cobra. O [`README.md`](../README.md) fala do produto; esta página fala de
como ele é feito.

> **Status:** a fundação técnica está de pé — design system, camada de dados offline-first, fluxo
> de autenticação completo, Security Rules testadas no emulador e CI com a `main` protegida. O
> produto em si — prescrição, execução e acompanhamento de treino — ainda não foi escrito. O que
> existe está listado em [O que já existe hoje](#o-que-já-existe-hoje); o restante desta página
> descreve o alvo, e diz quando o item ainda é alvo.

---

## Stack

| Camada | Tecnologia | Situação |
|---|---|---|
| Linguagem / UI | Kotlin · Jetpack Compose · Material 3 | Em uso |
| Arquitetura | MVVM com repositórios | Em uso |
| Persistência local | Room (**fonte de verdade da UI**) | Em uso |
| Backend | Cloud Firestore · Firebase Auth | Em uso |
| Observabilidade | Crashlytics · Analytics | Em uso |
| Configuração remota / kill-switch | Firebase Remote Config | Em uso |
| CI | GitHub Actions | Em uso |
| Sincronização | WorkManager | Planejado |
| Mídia de exercício | Cloudflare R2 | Planejado |
| Arquivos privados do usuário | Firebase Cloud Storage | Planejado |
| Push | Firebase Cloud Messaging | Planejado |
| Antiabuso | Firebase App Check (Play Integrity) | Planejado |
| Backend sob demanda | Cloud Functions | Planejado |

**Build:** Gradle 9.7 · AGP 9.3.1 · Kotlin 2.4.10 · compileSdk 37 · targetSdk 37 · minSdk 26 ·
toolchain JDK 21 provisionada automaticamente.

---

## O que já existe hoje

Quatro módulos, com a dependência andando em um sentido só.

### `:core` — design system

Tokens de marca `internal` (`Color.kt`), os dois `ColorScheme` espelhados entre claro e escuro, e
`ExtendedColorScheme` com os papéis que o Material 3 não tem: `ok` / `attention` / `critical` — o
semáforo de aderência — e `highlight`, para recordes. Mais `AppTypography`, `AppShapes`, a grade de
espaçamento em `Dimens` e o alvo de toque mínimo de 48 dp.

Seis componentes: `AppButton`, `AppTextField`, `AppMaskedTextField`, `AppCheckboxField`,
`AppNoticeCard` e `AppTopBar`. A máscara do campo mascarado é **posicional** — `#` é dígito, `A` é
letra, e uma letra digitada onde vai dígito não entra —, e o estado guarda só o conteúdo, nunca os
separadores.

`:core` não tem `strings.xml` por decisão: todo componente recebe o texto por parâmetro, então o
design system nunca escolhe idioma. As regras que regem essa camada estão em
[Design system](#design-system), mais abaixo.

### `:data` — Room como fonte de verdade

Banco Room com o catálogo de exercícios e a versão da última sincronização, fonte remota do
catálogo no Firestore, e `OfflineFirstExerciseRepository` como implementação de referência das
quatro regras da camada: leitura nunca toca a rede, sincronização é chamada explícita, falha de
rede é valor de retorno e não exceção, e todo acesso à rede declara seu custo de leitura no KDoc.

Também aqui: `FirebaseAuthRepository` e `FirestoreUserRepository`, os tipos de domínio (`UserProfile`,
`UserRoles`, `ActiveRole`, `PrivacyConsent`, `Exercise`) e o `DataContainer`, que é o único jeito de
construir um repositório de fora. Entidades, DAOs e fontes de dados são `internal`. Detalhes em
[`data/README.md`](../data/README.md).

### `:feature-auth` — autenticação completa

Seis fluxos, um pacote cada: boas-vindas, entrar, criar conta, recuperar senha, concluir cadastro e
escolher papel. O caminho é linear — boas-vindas → entrar → criar conta —, e **o papel é escolhido
antes de autenticar**, viaja como argumento de navegação e é gravado pelo cadastro, para ninguém ser
perguntado duas vezes.

Uma única tela de cadastro serve aos dois papéis, e o papel muda exatamente três coisas: a
finalidade declarada no texto de apoio de cada campo, o bloco entre contato e aceite (aviso de dado
de saúde para o aluno, registro CREF para o treinador) e se o telefone é obrigatório — é, para
treinador. O CREF é exigido porque prescrever exercício é atividade privativa de profissional
registrado (Lei 9.696/1998).

Contas criadas pelo Google chegam autenticadas e **incompletas** — sem nascimento, sem CREF, sem
aceite. `ProfileCompletion` diz o que falta, e a mesma checagem roda na abertura do app, então
fechar à força não é atalho para pular a conclusão.

Cadastro **não coleta dado de saúde**: isso é a anamnese, e vive em `students/{uid}`.

### `:app` — navegação e composição

`MainActivity` com Splash Screen API, `MainViewModel` resolvendo o destino inicial **antes** de o
`NavHost` ser composto (para o app nunca abrir na tela errada por um frame), `AppContainer` com
injeção manual e o grafo raiz com os três grafos irmãos: `auth`, `trainer` e `student`.

### Security Rules

`firestore/firestore.rules` nega por padrão, com **27 testes** cobrindo acesso do treinador ao
aluno, sessões de treino, máquina de estados do vínculo, catálogo, painel, trilha de auditoria,
perfil profissional e coleção não declarada.

### O que ainda não existe

Nada do produto em si: catálogo navegável, montagem de programa, atribuição, execução de treino,
histórico, painel do treinador, mensagens e avaliações. Junto com eles vêm a fila durável de
escrita (WorkManager), o App Check, o push e as Cloud Functions.

---

## Como rodar

Requer Android Studio recente e um dispositivo ou emulador com API 26+. Não é necessário configurar `JAVA_HOME`: a JVM do daemon é provisionada pelo Gradle via foojay.

```powershell
.\gradlew.bat assembleDebug      # gera o APK de debug
.\gradlew.bat installDebug       # instala no dispositivo conectado
.\gradlew.bat test               # testes unitários (JVM) — 121 hoje
.\gradlew.bat lint               # Android Lint + compose-lints
.\gradlew.bat koverHtmlReport    # cobertura do :data, sem mínimo exigido por decisão
```

Em Linux/macOS, use `./gradlew` no lugar de `.\gradlew.bat`.

Rodar um teste específico:

```powershell
.\gradlew.bat testDebugUnitTest --tests "*.SignUpViewModelTest"
.\gradlew.bat testDebugUnitTest --tests "*.feature.auth.recovery.*"
```

Os nomes de método são frases entre crases (`fun \`aluno nao precisa de celular nem de registro\`()`),
então filtrar por método exige aspas em volta do padrão inteiro — na dúvida, filtre pela classe.

### Firebase

O `google-services.json` **não está no repositório** — o projeto é público e o arquivo carrega
`project_id` e API key (o porquê está no [ADR-0004](adr/0004-configuracao-do-firebase.md)).
Sem ele o build continua funcionando, apenas sem Firebase, e emite um aviso.

Para habilitar:

1. No [console do Firebase](https://console.firebase.google.com), abra o projeto (ou crie um).
2. Registre um app Android com o pacote `com.gabrielfreire.runandlift`.
3. Baixe o `google-services.json` e salve em `app/google-services.json`.
4. No console, habilite os produtos que a Fase 1 usa: **Firestore**, **Authentication**
   (e-mail/senha e Google), **Crashlytics**, **Analytics**, **Remote Config** e **Performance**.

Ao criar o Firestore, três escolhas são **permanentes**: edição **Standard**, ID do banco
`(default)` e local `southamerica-east1`. O porquê está no
[ADR-0005](adr/0005-firestore-edicao-e-banco-padrao.md).

Em builds de debug, a coleta de Crashlytics, Analytics e Performance fica desligada, e a
instrumentação do Performance nem roda — dados de desenvolvimento contaminariam o crash-free rate,
o funil e a medição de tempo de tela.

### Alerta de regressão de crash-free rate

O SDK já reporta em builds de release. O **alerta** é configuração de console, feita uma vez em
**Crashlytics › ⋮ › Configurações de alertas**:

| Alerta | Ligar? | Observação |
|---|---|---|
| **Velocity alerts** | Sim | Avisa quando um problema novo passa a afetar uma fatia relevante das sessões. É o que pega regressão cedo |
| **Regression alerts** | Sim | Avisa quando um problema fechado volta a ocorrer |
| **New issue alerts** | Sim, no piloto | Com poucos usuários o volume é baixo. Reavaliar no beta fechado, quando pode virar ruído |

Destino: e-mail do mantenedor. O número que importa é o **crash-free rate acima de 99,5%** — é
critério de saída do primeiro marco, e a razão de a coleta em debug estar desligada.

O `assembleRelease` hoje sai com R8 desligado, então as stack traces já vêm legíveis. Quando o R8
for ligado, o upload do arquivo de mapeamento passa a ser necessário para o relatório continuar
inteligível.

### Qualidade de código

Ative o hook de pré-commit uma vez, logo após clonar — ele formata e analisa antes de deixar o
commit passar:

```bash
git config core.hooksPath .githooks
```

Esse passo é por clone e por pessoa: o Git não versiona `.git/hooks`, então não há como ativá-lo
automaticamente. O hook é conveniência e pode ser pulado com `--no-verify`; a garantia real é o CI,
que roda `spotlessCheck detekt lint test` em todo push e pull request para `main`.

```powershell
.\gradlew.bat spotlessApply     # formata (ktlint)
.\gradlew.bat spotlessCheck     # verifica formatação, sem escrever
.\gradlew.bat detekt            # análise estática
```

Não há baseline do detekt: a contagem de violações é zero e deve continuar assim.

As regras de formatação ficam no `.editorconfig`, que é lido tanto pela IDE quanto pelo build —
não há chave duplicada em `build.gradle.kts`. As regras de complexidade ficam em
`config/detekt/detekt.yml`, e as exceções do Android Lint e do compose-lints em `core/lint.xml`,
escopadas por caminho em vez de desligadas globalmente: uma regra que atrapalha dentro do design
system em geral continua certa no código de tela.

Os limites que o detekt cobra em código novo: **6 parâmetros** por função e **7** por construtor
(padrões não contam, então API de slot do Compose passa), **11 funções** por arquivo e por classe,
**60 linhas** por função e **120 colunas**.

### Security Rules do Firestore

As regras vivem em `firestore/firestore.rules` e negam por padrão. Os testes rodam contra o
emulador:

```bash
cd firestore
npm ci
npm test
```

O emulador é um processo Java — se `java` não estiver no PATH, ele não sobe. O CI roda esses
testes em job próprio, em paralelo ao build Android.

Para publicar as regras no projeto real: `npx firebase deploy --only firestore:rules`.

### Proteção da branch main e fluxo de trabalho

O CI só vira garantia se não puder ser contornado. A ruleset **já está ativa** sobre a `main` (o
porquê está no [ADR-0015](adr/0015-protecao-da-branch-principal.md)) e exige:

| Regra | Efeito |
|---|---|
| Pull request obrigatório | Nada entra na `main` por push direto |
| Checks `verify` e `firestore-rules` | Os dois **job ids** do `ci.yml` — renomear um job sem atualizar a ruleset trava todo PR seguinte |
| Uma aprovação de code owner | `.github/CODEOWNERS` dá o dono a todos os caminhos |
| Histórico linear | Merge por squash |
| Sem force push e sem deleção | Vale para todo mundo |

A exigência de revisão existe pelos PRs que ninguém está olhando — os do **Dependabot**, que
chegam sem revisor. O GitHub não pede revisão ao autor do próprio PR nem deixa aprová-lo, então
nos PRs do dono a exigência fica sem como ser cumprida e o merge sai pelo bypass de administrador,
que a ruleset concede. Isso é intencional: tirar a exigência devolveria os PRs de bot ao estado de
ninguém ser chamado.

O ciclo de trabalho, então, é sempre: branch a partir da `main` atualizada → commits → PR → volta
para a `main`. As labels são um vocabulário fixo, e um PR leva quantas couberem:

`refactor` · `feature` · `fix` · `tests` · `docs` · `build` · `security`

Mensagens de commit e descrições de PR são escritas em **português**, como o KDoc; código e
identificadores ficam em inglês.

### Atualização de dependências

`.github/dependabot.yml` abre PRs semanais agrupados (toolchain, Compose, androidx, ferramentas de
qualidade) e mensais para as actions. Eles chegam com a label `build` e pedindo revisão do dono via
CODEOWNERS. Quem valida é o CI. Nada é mesclado automaticamente.

Duas armadilhas do arquivo, já resolvidas nele: a chave `labels:` **substitui** as labels padrão em
vez de somar a elas, e exige que a label já exista — o Dependabot não cria as customizadas. E ele lê
a configuração sempre do branch padrão, então mudança em `dependabot.yml` só vale depois do merge.

---

## Arquitetura

### Estrutura de módulos

```
:app           — aplicação, grafo raiz de navegação, AppContainer (injeção manual)
:core          — design system: tema, tipografia, componentes
:data          — Room, Firestore, repositórios
:feature-auth  — boas-vindas, entrar, criar conta, recuperar senha, concluir cadastro, escolher papel
:feature-*     — demais funcionalidades, criadas sob demanda
```

Dentro de um módulo de feature, **um pacote por contexto**: cada fluxo é dono do seu, incluindo o
`…Destination.kt` que liga a tela ao ViewModel, de modo que o arquivo do grafo continue sendo só um
mapa de rotas. Pacote compartilhado existe quando dois fluxos de fato compartilham — e o nome diz
quais, como `credentials/` (entrar + criar conta) e `profileform/` (criar conta + concluir cadastro).

A preferência geral é por **muitos arquivos pequenos em vez de poucos misturados**: um tipo público
por arquivo, um composable por arquivo com o seu próprio `@Preview`, e a extensão de um tipo dentro
do arquivo do tipo que ela estende.

A dependência anda em um sentido só: `:app` e `:feature-*` dependem de `:core` e `:data`; `:core`
não depende de ninguém. Módulo de feature nunca depende do `:app` — os repositórios chegam por
parâmetro na função do grafo.

A navegação é composta por **grafos irmãos por papel** (`auth`, `trainer`, `student`), e não por um
grafo único com condicionais: tela de treinador não existe na pilha de um aluno.

O papel é escolhido **antes do login**, na tela de boas-vindas, e gravado pelo cadastro — quem já
tem conta apenas tem o papel lido. O porquê está no
[ADR-0010](adr/0010-escolha-de-papel-antes-do-login.md).

O padrão de apresentação é MVVM: o ViewModel expõe estado como `StateFlow` somente-leitura, a
mutação fica restrita a ele, e nenhuma referência a `Context` ou tipo de UI entra ali.

### Design system

Vive em `:core`, no pacote `core.designsystem`, e é consumido sempre pela camada mais alta: `MaterialTheme.colorScheme` para
os papéis do Material 3, `MaterialTheme.extendedColors` para os papéis que ele não tem, e
`AppTypography` / `AppShapes` / `Dimens` para o resto. Os tokens de marca são `internal` e nenhuma
tela os referencia direto — trocar a paleta é reescrever um arquivo.

Três regras que são decisão, não preferência:

- **Sem cor dinâmica (Material You).** As cores de estado precisam significar o mesmo em qualquer
  aparelho, e cor dinâmica repintaria tanto elas quanto a identidade.
- **Cor nunca é o único canal de informação.** Todo estado sinalizado por cor acompanha ícone e
  rótulo textual. Contraste mínimo AA, alvo de toque mínimo de 48 dp, texto sempre em `sp`.
- **Números medidos usam dígitos tabulares** (`MetricTextStyles`), para o layout não deslocar
  conforme o valor muda.

`ThemePreviews.kt` é uma galeria de todos os papéis de cor e estilos de texto nos dois temas —
o lugar para conferir antes e depois de mexer em qualquer token.

### Offline-first de verdade

O aplicativo é usado dentro de academias, onde a rede frequentemente não existe. Três decisões sustentam isso e não devem ser contornadas:

1. **Room é a fonte de verdade da UI.** Nenhuma tela lê o Firestore diretamente; o Firestore sincroniza, o Room é lido. Cache que "às vezes funciona" não atende ao requisito.
2. **Toda escrita passa por uma fila durável** (WorkManager) com `clientWriteId` idempotente e retry exponencial. Um treino registrado sem rede não pode se perder. *Ainda não implementada — chega junto da primeira escrita de treino.*
3. **Resolução de conflito é last-write-wins por campo, com uma exceção:** séries executadas pelo aluno nunca são sobrescritas por sincronização.

### Orçamento de leitura do Firestore

Leitura do Firestore é o recurso escasso do projeto e é tratada como orçamento, não como detalhe de implementação. Cinco regras valem para qualquer código novo:

1. **Documento-resumo em vez de varredura.** Telas agregadas leem um documento pré-computado, não N documentos.
2. **Agregados embutidos.** Uma sessão de treino é um documento único com as séries dentro — 1 escrita por treino concluído, não 30.
3. **Cache primeiro.** `Source.CACHE` com fallback para servidor, exceto onde a atualidade for essencial.
4. **Nenhum listener em coleção que cresce sem limite.** Listener só em documento único ou query com `limit()`.
5. **Catálogo de exercícios versionado e cacheado no Room**, revalidado por número de versão no Remote Config.

Todo item de trabalho declara seu custo de leitura antes de ser implementado.

### Modelo de dados

Coleções principais no Firestore: `users`, `trainerProfiles`, `students`, `links`, `inviteCodes`, `exercises`, `programs`, `assignments`, `sessions`, `studentSummaries`, `trainerDashboards`, `threads`, `reviews`, `assessments`, `auditLogs`.

O papel duplo é resolvido no modelo: um mesmo `uid` pode ter documento em `trainerProfiles` **e** em `students`, com `activeRole` decidindo qual grafo de navegação é montado. Não há segunda conta nem segundo login.

O vínculo entre treinador e aluno vive em `links`, uma coleção de topo (consultável pelos dois lados), com máquina de estados `invited | requested | active | paused | ended`. Encerrar um vínculo nunca apaga dados: o aluno mantém acesso permanente ao próprio histórico.

O documento de vínculo tem **id obrigatoriamente no formato `{trainerId}_{studentId}`**. Não é
estética: Security Rule não consulta, só faz `get()` por caminho exato, e é esse formato que torna
"treinador só lê aluno com vínculo ativo" expressável ([ADR-0007](adr/0007-security-rules-e-id-de-vinculo-deterministico.md)).

Dessas coleções, as que já têm código escrevendo ou lendo são `users`, `trainerProfiles` e
`exercises`; as demais existem hoje como Security Rules e seus testes, à espera das telas.

### Acesso e privacidade

- Security Rules por papel **e** por estado do vínculo — um treinador só lê dados de aluno com `link.status == active`.
- Dado de saúde (anamnese, fotos de progresso) tem regras estritas e consentimento próprio.
- Trilha de auditoria imutável para prescrição e para acesso a dado sensível.
- Exclusão de conta e exportação completa de dados são funcionalidades do app, não processos manuais.

---

## Testes

| Camada | O que cobre | Ferramenta | Situação |
|---|---|---|---|
| Unitário | Transições de estado dos ViewModels, regras de validação, montagem de rota, mapeamento formulário → armazenamento, repositório cache-first | JUnit | **121 testes** |
| Regras | Security Rules por papel e estado de vínculo | `@firebase/rules-unit-testing` | **27 testes** |
| Integração | Room ↔ Firestore, resolução de conflito | Firebase Emulator Suite | Planejado |
| Manual | Cenários de rede ruim e perda de conexão | Dispositivo real | Planejado |

**Não há teste de UI, e isso é decisão.** Layout se confere abrindo o `@Preview` — é para isso que
todo arquivo de Compose carrega um. O que ganha teste é o que o preview não mostra. Os fakes são
escritos à mão, não gerados por MockK, e ficam em `feature-auth/src/test/…/fake/` para serem
reusados em vez de reescritos dentro de cada teste.

Os testes que mais importam são os de regra invisível na tela e fácil de desfazer sem querer: a
recuperação de senha respondendo **igual** para e-mail que existe e que não existe, a conclusão de
cadastro respondendo "não falta nada" quando a leitura falha — ninguém fica bloqueado por um palpite
— e os quatro destinos iniciais do app, em ordem.

### Definição de pronto

1. Compila, passa no lint e nos testes existentes.
2. Comportamento especificado nos **dois papéis** quando a tela for compartilhada.
3. Funciona offline ou degrada de forma explícita, com mensagem clara.
4. Custo de leitura do Firestore medido e dentro do declarado.
5. Security Rules cobrindo o dado novo, com teste no emulador.
6. Evento de Analytics disparado quando o item fizer parte de um funil monitorado.
7. Sem regressão no crash-free rate.
8. Se tocar dado de saúde: base legal identificada e consentimento coberto.

---

## Convenções

- **Versões de dependência só no catálogo** (`gradle/libs.versions.toml`), referenciadas como `libs.*`. Repositórios são declarados apenas em `settings.gradle.kts` (`FAIL_ON_PROJECT_REPOS`).
- **Configuration cache está ligado** — build logic precisa ser compatível.
- **Acessibilidade não é opcional:** alvos de toque ≥ 48 dp e contraste mínimo AA.
- **Sem anúncios**, em nenhuma tela, em nenhuma versão — nem SDK de rede de anúncios desativado
  atrás de flag. Decisão registrada em [`docs/adr/0008`](adr/0008-zero-anuncio.md), com as
  alternativas descartadas e o único cenário que justifica reabrir.

As decisões cujo motivo o código não carrega estão em [`docs/adr/`](adr/README.md) — hoje são
quinze, da escolha das ferramentas de qualidade à proteção da `main`, passando pela navegação por
papel e pelo cadastro de treinador. ADR não se reescreve: decisão revista vira um ADR novo.

Orientações para agentes de IA que trabalham neste repositório estão em [`CLAUDE.md`](../CLAUDE.md).

A licença vale para tudo o que está aqui: software proprietário, todos os direitos reservados. Ver
[`LICENSE`](../LICENSE).
