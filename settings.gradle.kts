pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RunAndLift"

// Estrutura do backlog E0-01. A dependência anda em um sentido só: :app -> :core, :app -> :data,
// :data -> :core. :core nunca depende de ninguém — é o que impede o design system de virar refém
// de regra de negócio.
//
// Os módulos de feature nascem junto com a primeira tela (E1-02), não antes: módulo vazio custa
// configuração de build e não entrega separação nenhuma.
//
// A raiz tem **três pastas de módulo e não mais**: core, data e feature. Cada feature é um módulo
// próprio dentro de `feature/`, e não um `:feature-nome` solto na raiz — com um módulo por tela o
// diretório do projeto passaria a listar dez pastas irmãs em que só o prefixo diz quem é do quê. O
// `:feature` em si não tem código nem build.gradle.kts: é só o guarda-chuva.
include(":app")
include(":core")
include(":data")
include(":feature:auth")
include(":feature:student")
include(":feature:trainer")
