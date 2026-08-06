# Run & Lift

Aplicativo Android nativo para prescrição e acompanhamento de treinos de musculação, com **papel duplo na mesma instalação**: a mesma conta pode atuar como treinador, como aluno, ou como ambos — o papel ativo determina o grafo de navegação inteiro.

> **Status:** em desenvolvimento inicial. O repositório está na fase de fundação técnica — o que existe hoje é o esqueleto do projeto Android. A arquitetura descrita abaixo é o alvo, não o estado atual.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem / UI | Kotlin · Jetpack Compose · Material 3 |
| Arquitetura | MVVM com repositórios |
| Persistência local | Room (**fonte de verdade da UI**) |
| Backend | Cloud Firestore · Firebase Auth |
| Sincronização | WorkManager |
| Mídia de exercício | Cloudflare R2 |
| Arquivos privados do usuário | Firebase Cloud Storage |
| Push | Firebase Cloud Messaging |
| Observabilidade | Crashlytics · Analytics |
| Configuração remota / kill-switch | Firebase Remote Config |
| Antiabuso | Firebase App Check (Play Integrity) |
| Backend sob demanda | Cloud Functions |
| CI | GitHub Actions |

**Build:** Gradle 9.5 · AGP 9.3.1 · Kotlin 2.4.10 · compileSdk 37 · minSdk 26 · toolchain JDK 21 provisionada automaticamente.

---

## Como rodar

Requer Android Studio recente e um dispositivo ou emulador com API 26+. Não é necessário configurar `JAVA_HOME`: a JVM do daemon é provisionada pelo Gradle via foojay.

```powershell
.\gradlew.bat assembleDebug      # gera o APK de debug
.\gradlew.bat installDebug       # instala no dispositivo conectado
.\gradlew.bat test               # testes unitários (JVM)
.\gradlew.bat connectedAndroidTest   # testes instrumentados e de UI
.\gradlew.bat lint               # Android Lint
```

Em Linux/macOS, use `./gradlew` no lugar de `.\gradlew.bat`.

Rodar um teste específico:

```powershell
.\gradlew.bat testDebugUnitTest --tests "com.gabrielfreire.runandlift.ExampleUnitTest"
.\gradlew.bat testDebugUnitTest --tests "*.ExampleUnitTest.addition_isCorrect"
```

### Firebase

O `google-services.json` **não está no repositório** — o projeto é público e o arquivo carrega
`project_id` e API key (o porquê está no [ADR-0004](docs/adr/0004-configuracao-do-firebase.md)).
Sem ele o build continua funcionando, apenas sem Firebase, e emite um aviso.

Para habilitar:

1. No [console do Firebase](https://console.firebase.google.com), abra o projeto (ou crie um).
2. Registre um app Android com o pacote `com.gabrielfreire.runandlift`.
3. Baixe o `google-services.json` e salve em `app/google-services.json`.
4. No console, habilite os produtos que a Fase 1 usa: **Firestore**, **Authentication**
   (e-mail/senha e Google), **Crashlytics**, **Analytics**, **Remote Config** e **Performance**.

Ao criar o Firestore, três escolhas são **permanentes**: edição **Standard**, ID do banco
`(default)` e local `southamerica-east1`. O porquê está no
[ADR-0005](docs/adr/0005-firestore-edicao-e-banco-padrao.md).

Em builds de debug, a coleta de Crashlytics, Analytics e Performance fica desligada, e a
instrumentação do Performance nem roda — dados de desenvolvimento contaminariam o crash-free rate,
o funil e a medição de tempo de tela.

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
`config/detekt/detekt.yml`, e as exceções do Android Lint e do compose-lints em `app/lint.xml`.

### Proteção da branch main

O CI só vira garantia se não puder ser contornado. Isso não é configurável por código — faça uma
vez, em **Settings › Rules › Rulesets › New branch ruleset**:

1. Target branches: `main`.
2. Marque **Require a pull request before merging** (com 0 aprovações, se você trabalha sozinho).
3. Marque **Require status checks to pass** e selecione o check `verify` do workflow CI. Ele só
   aparece na lista depois que o workflow tiver rodado ao menos uma vez.
4. Marque **Block force pushes**.
5. Deixe **Do not allow bypassing the above settings** desmarcado se quiser poder destravar você
   mesmo em emergência — marcado, nem você passa por cima.

### Atualização de dependências

`.github/dependabot.yml` abre PRs semanais agrupados (toolchain, Compose, androidx, ferramentas de
qualidade) e mensais para as actions. Quem valida é o CI. Nada é mesclado automaticamente.

---

## Arquitetura

### Estrutura de módulos

```
:app          — aplicação, MainViewModel, AppContainer (injeção manual)
:core         — design system, utilitários, contratos comuns
:data         — Room, Firestore, repositórios, fila de sincronização (ainda vazio)
:feature-*    — uma funcionalidade por módulo (criados sob demanda, a partir da primeira tela)
```

A dependência anda em um sentido só: `:app → :core`, `:app → :data`, `:data → :core`. `:core` não
depende de ninguém — é o que impede o design system de virar refém de regra de negócio.

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
2. **Toda escrita passa por uma fila durável** (WorkManager) com `clientWriteId` idempotente e retry exponencial. Um treino registrado sem rede não pode se perder.
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

### Acesso e privacidade

- Security Rules por papel **e** por estado do vínculo — um treinador só lê dados de aluno com `link.status == active`.
- Dado de saúde (anamnese, fotos de progresso) tem regras estritas e consentimento próprio.
- Trilha de auditoria imutável para prescrição e para acesso a dado sensível.
- Exclusão de conta e exportação completa de dados são funcionalidades do app, não processos manuais.

---

## Testes

| Camada | O que cobre | Ferramenta |
|---|---|---|
| Unitário | Repositórios, fila de sincronização, cálculo de aderência | JUnit + Turbine |
| Integração | Room ↔ Firestore, resolução de conflito | Firebase Emulator Suite |
| Regras | Security Rules por papel e estado de vínculo | `@firebase/rules-unit-testing` |
| Instrumentação | Fluxos de execução de treino | Compose UI Test |
| Manual | Cenários de rede ruim e perda de conexão | Dispositivo real |

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
- **Sem anúncios**, em nenhuma tela, em nenhuma versão.

Orientações para agentes de IA que trabalham neste repositório estão em [`CLAUDE.md`](CLAUDE.md).

---

## Licença

Software proprietário. **Todos os direitos reservados** — veja [`LICENSE`](LICENSE).

Este repositório é público apenas para consulta e transparência. Nenhum direito de uso, cópia, modificação, distribuição ou publicação é concedido. Para qualquer uso, entre em contato com o titular.
