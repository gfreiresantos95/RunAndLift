# ADR-0002: JDK 21 no daemon do Gradle

- **Status:** Aceito
- **Data:** 2026-08-06

## Contexto

O `gradle/gradle-daemon-jvm.properties` foi gerado por `updateDaemonJvm` com `toolchainVersion=25`
— padrão da ferramenta, não requisito do projeto.

Ao introduzir o detekt 1.23.8, a tarefa falhou antes de analisar qualquer linha:

```
java.lang.IllegalArgumentException: 25.0.2
    at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse
    at org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment.<init>
```

O compilador Kotlin embutido no detekt 1.23.x não consegue parsear a versão da JVM em execução.
Três fatos apurados: a tarefa `Detekt` não expõe `javaLauncher`, ou seja, roda obrigatoriamente na
JVM do daemon; 1.23.8 é a última release da linha 1.x; e a única JDK da máquina era 25 — a própria
JBR do Android Studio.

## Decisão

Fixar o daemon do Gradle em **JDK 21** (LTS), regenerando o arquivo com
`./gradlew updateDaemonJvm --jvm-version=21` para que as URLs do foojay acompanhem a versão. O CI
usa Temurin 21 pelo mesmo motivo.

## Alternativas consideradas

**Manter 25 e usar detekt 2.x.** Rejeitado: 2.x está em alpha (ver ADR-0001).

**Manter 25 e ficar sem detekt.** Rejeitado: a 1.x está em manutenção e não vai ganhar suporte a
JDK 25, então na prática seria abrir mão de limite de complexidade por tempo indeterminado.

**Rodar o detekt via `JavaExec` numa toolchain separada.** Rejeitado: exigiria o `detekt-cli` como
dependência extra e substituir a tarefa do plugin por uma artesanal — complexidade de build para
contornar uma incompatibilidade temporária.

## Consequências

Nada muda no produto: a JDK do daemon não afeta o APK. O bytecode continua vindo de
`compileOptions` (Java 11), e as features de linguagem, do Kotlin 2.4.10. Verificado depois da
troca: `assembleDebug`, `test` e `lint` passam no AGP 9.3.1 com JDK 21.

O custo é ambiental: quem clonar baixa uma JDK 21 (~200 MB, via foojay, automático) diferente da
JBR do Android Studio, e o CI faz o mesmo. Se algum plugin futuro exigir 25+, o impasse volta.

O build inteiro está acomodado à limitação de uma ferramenta só. É um preço aceitável enquanto for
reversível em uma linha — e é.

## Quando revisitar

Quando o detekt 2.x estabilizar. Aí `updateDaemonJvm --jvm-version=<atual>` e o alinhamento do
`java-version` no `ci.yml` encerram esta decisão.
