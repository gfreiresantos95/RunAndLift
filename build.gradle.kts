import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

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

// Kover mede o projeto inteiro, e não só o :data — dos 44 arquivos de teste, 37 vivem nos módulos
// de feature, que ficavam de fora da agregação e portanto do relatório.
dependencies {
    kover(project(":app"))
    kover(project(":core"))
    kover(project(":data"))
    kover(project(":feature:auth"))
    kover(project(":feature:student"))
    kover(project(":feature:trainer"))
}

kover {
    reports {
        filters {
            excludes {
                // Não há teste de UI por decisão (ver a seção Testing do CLAUDE.md): a tela se
                // confere pelo @Preview. Sem esta exclusão, todo composable entraria como zero e o
                // percentual passaria a medir uma decisão documentada em vez de uma lacuna.
                annotatedBy("androidx.compose.runtime.Composable")

                // Sobra do compilador do Compose: as classes que guardam as lambdas de composable
                // não carregam a anotação e escapariam do filtro acima.
                classes("*ComposableSingletons*")

                // R, BuildConfig e Manifest — código gerado pelo AGP, que ninguém escreve nem testa.
                androidGeneratedClasses()

                // DAOs e banco gerados pelo Room via KSP. São 300 linhas que ninguém escreveu, e
                // testá-las seria testar o Room. O que é nosso na persistência — a query e o
                // esquema — se verifica por teste de migração, não por cobertura de linha.
                classes("*_Impl", "*_Impl\$*")

                // Dados de exemplo dos @Preview: existem para desenhar a tela, não rodam em produção.
                classes("*PreviewFixtures*")

                // Tokens do design system: rampa de cor, tipografia, formas, espaçamento e duração.
                // São declarações constantes, sem ramo nem decisão; o teste possível repetiria o
                // literal do fonte. A verificação real é a galeria ThemePreviews, em light e dark.
                classes(
                    "com.gabrielfreire.runandlift.core.designsystem.ColorKt",
                    "com.gabrielfreire.runandlift.core.designsystem.ColorSchemeKt",
                    "com.gabrielfreire.runandlift.core.designsystem.ExtendedColorSchemeKt",
                    "com.gabrielfreire.runandlift.core.designsystem.TypeKt",
                    "com.gabrielfreire.runandlift.core.designsystem.ShapeKt",
                    "com.gabrielfreire.runandlift.core.designsystem.Dimens",
                    "com.gabrielfreire.runandlift.core.designsystem.AppMotion",
                    "com.gabrielfreire.runandlift.core.designsystem.AppIcons",
                )

                // Adaptadores do Firestore **sem regra dentro**, nomeados um a um.
                //
                // O ADR-0018 rejeitou excluir a camada de adaptadores inteira, e essa rejeição
                // continua de pé: ela esconderia a trava de consentimento de saúde da
                // `FirestoreStudentRepository`, que é regra de LGPD sem teste. O que muda com o
                // ADR-0021 é o critério — entra nesta lista o adaptador de quem **já tirou de
                // dentro de si tudo o que decidia**, e a coisa extraída tem teste próprio. A
                // exclusão passa a ser o prêmio por extrair a lógica, e não o esconderijo dela.
                //
                // Requisito para acrescentar um nome aqui: apontar, no PR, onde mora a regra que
                // saiu e qual teste a afirma. Sem esse par, o nome não entra.
                classes(
                    // Regras extraídas: LinkDocument (id e mapas), LinkRequest (criar, reabrir ou
                    // recusar) e InviteCodeDocument (alfabeto e normalização), com teste cada.
                    "com.gabrielfreire.runandlift.data.link.FirestoreLinkRepository",
                    "com.gabrielfreire.runandlift.data.link.FirestoreLinkRepository\$*",
                    "com.gabrielfreire.runandlift.data.link.FirestoreInviteCodes",
                    "com.gabrielfreire.runandlift.data.link.LinkSnapshotKt",
                )
            }
        }

        // O piso vale a partir da branch, não só do CI: quem roda `./gradlew koverVerify` antes de
        // abrir o PR descobre em casa. 60 e não 75 porque a medição de agosto/2026 deu 63,4% — piso
        // acima do real deixaria o main vermelho e faria a cobertura pautar o roadmap. Ver ADR 0018.
        verify {
            rule("Piso de cobertura do projeto") {
                bound {
                    minValue = 60
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
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
