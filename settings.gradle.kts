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
// Os módulos :feature-* nascem junto com a primeira tela (E1-02), não antes: módulo vazio custa
// configuração de build e não entrega separação nenhuma.
include(":app")
include(":core")
include(":data")
include(":feature-auth")
