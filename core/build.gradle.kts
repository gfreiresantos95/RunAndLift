plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.gabrielfreire.runandlift.core"

    compileSdk {
        version = release(version = 37)
    }

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // `api` e não `implementation`: quem consome :core desenha com Compose e Material 3. Esconder
    // essas dependências obrigaria cada módulo de tela a redeclarar as mesmas linhas.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)

    // O design system inteiro mora aqui, então as regras do compose-lints precisam rodar aqui.
    lintChecks(libs.compose.lint.checks)

    // O design system se confere pela galeria de previews, com uma exceção: mapeamento de cursor
    // de máscara é aritmética, e aritmética errada aqui vira exceção em tempo de execução.
    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
