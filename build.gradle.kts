import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    // Declarados aqui com `apply false` só para fixar a versão no classpath da build. Sem isto,
    // o módulo que pedir `alias(libs.plugins.android.library)` com versão colide com o AGP que já
    // veio pelo `android.application`.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// Spotless roda a partir da raiz e cobre o repositório inteiro, inclusive app — os alvos são
// resolvidos em relação ao diretório do projeto onde o plugin foi aplicado.

/**
 * Lê as seções indicadas do. Editorconfig e devolve as propriedades como mapa.
 *
 * Existe porque o Spotless não entrega o. Editorconfig ao ktlint: ele formata uma string em
 * memória, então não há arquivo em disco de onde subir procurando o `.editorconfig`, e o
 * `setEditorConfigPath` não surte efeito nesta combinação de versões (verificado com
 * max_line_length e com chaves `ktlint_standard_*`, nas duas seções). O único canal que o ktlint
 * respeita é o `editorConfigOverride`.
 *
 * Em vez de manter as chaves em dois arquivos e torcer para não divergirem, o override é
 * alimentado a partir do próprio. Editorconfig. Ele continua a ser a fonte de verdade única: a
 * IDE lê direto, a build lê por aqui.
 *
 * As seções são aplicadas na ordem informada, então a mais específica deve vir por último.
 */
fun editorConfigProperties(vararg sections: String): Map<String, String> {
    val properties = linkedMapOf<String, String>()
    var section = ""

    rootProject.file(".editorconfig").readLines().forEach { rawLine ->
        val line = rawLine.substringBefore('#').substringBefore(';').trim()

        when {
            line.isEmpty() -> Unit

            line.startsWith("[") && line.endsWith("]") -> section = line.removeSurrounding("[", "]")

            section in sections && "=" in line -> {
                val (key, value) = line.split("=", limit = 2)

                properties[key.trim()] = value.trim()
            }
        }
    }

    return properties
}

val ktlintSettings = editorConfigProperties("*", "*.{kt,kts}")

// Kover: apenas relatório, sem piso de cobertura. Percentual mínimo em projeto nesta fase
// premiaria teste de getter e não diria nada sobre a política que realmente importa. O relatório
// serve para enxergar o que ficou descoberto, não para barrar build.
dependencies {
    kover(project(":data"))
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintSettings)
        endWithNewline()
        trimTrailingWhitespace()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintSettings)
    }
}

// Detekt, ao contrário do Spotless, analisa os source sets de cada projeto — então precisa ser
// aplicado por módulo. Sem isto, './gradlew detekt' na raiz não olharia nenhuma linha de app.
val detektConfigFile = files("$rootDir/config/detekt/detekt.yml")

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        // Parte do default do detekt e sobrepõe só o que está em detekt.yml, em vez de o YAML
        // ter que redeclarar todas as regras.
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(detektConfigFile)
    }

    // O detekt 1.23.x embute um compilador Kotlin que não conhece jvm-target acima de 22, e por
    // padrão ele usa a versão da JVM que roda o daemon (25, por gradle-daemon-jvm.properties).
    // Fixar no mesmo alvo do 'compileOptions' do app resolve e mantém os dois coerentes.
    tasks.withType<Detekt>().configureEach {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}
