# ADR-0001: Ferramentas de qualidade e formatação

- **Status:** Aceito
- **Data:** 2026-08-06

## Contexto

O projeto está em fase de fundação: um módulo (`:app`), Compose, sem telas. O momento de fixar
formatação e limites de complexidade é antes do primeiro commit de código real — depois, qualquer
regra nova gera um diff gigante que ninguém revisa e uma tentação permanente de `--no-verify`.

A restrição de partida foi deliberada: ligar poucas regras bem escolhidas, e não tudo que as
ferramentas oferecem.

## Decisão

- **Spotless com ktlint** para formatação, aplicado da raiz sobre o repositório inteiro.
- **detekt** para complexidade e análise estática, com `buildUponDefaultConfig` e ajustes em
  `config/detekt/detekt.yml`.
- **compose-lints (Slack)** via `lintChecks`, com exceções por caminho em `app/lint.xml`.
- **Sem baseline** em nenhuma das três. A contagem de violações é zero e é assim que fica.
- **Configuração da IDE versionada** (`.idea/codeStyles`, `inspectionProfiles`, `compiler.xml`,
  `runConfigurations.xml`), com o resto de `.idea/` ignorado.
- O CI (`.github/workflows/ci.yml`) é a garantia real; o hook em `.githooks/pre-commit` é
  conveniência e pode ser pulado.

## Alternativas consideradas

**ktlint direto, sem Spotless.** Descartado: o Spotless dá `apply` e `check` como tarefas
separadas, o que é exatamente a divisão entre hook local (corrige) e CI (verifica).

**detekt 2.x (`dev.detekt`).** Descartado por estar em alpha. Custou caro: a 1.23.x não roda em
JDK 25, o que forçou o ADR-0002.

**Baseline para começar sem atrito.** Descartado: o projeto tem nove arquivos Kotlin. Baseline
aqui seria dívida nascida pronta. As 77 violações iniciais foram resolvidas na origem — duas por
correção de código (imports com curinga), o resto por configuração de regra justificada em
comentário no YAML.

**Estilo `android_studio` do ktlint em vez de `ktlint_official`.** Descartado: o estilo do Android
Studio remove vírgulas finais, que o Compose usa por convenção e que reduzem ruído de diff.

**Regras de nomenclatura padrão.** Ajustadas, não descartadas: `@Composable` é PascalCase por
convenção oficial do Compose, e sem a exceção toda tela do projeto viraria violação.

## Consequências

Código novo já nasce dentro dos limites: 6 parâmetros por função, 7 por construtor (defaults não
contam), 11 funções por arquivo e por classe, 60 linhas por função, 120 colunas.

Fica uma armadilha documentada: **o Spotless não lê as chaves `ktlint_*` do `.editorconfig`** —
entrega ao ktlint uma string em memória, sem caminho em disco de onde descobrir o arquivo, e nem
`setEditorConfigPath` resolve nesta combinação de versões. Por isso `editorConfigProperties()` em
`build.gradle.kts` lê o `.editorconfig` e alimenta o `editorConfigOverride`. Quem mexer nessa
função sem entender o motivo vai reintroduzir a duplicação de chaves.

Cada exceção de regra carrega o porquê em comentário, no `detekt.yml` ou no `lint.xml`. Exceção
sem justificativa é dívida silenciosa.

## Quando revisitar

Quando o detekt 2.x sair de alpha — ele destrava o retorno à JDK atual (ADR-0002) e encerra a
dívida de deprecação do detekt 1.23.x com o Gradle 10. Ou quando o projeto ganhar um segundo
módulo, momento em que a configuração precisa sair da raiz e virar convention plugin.
