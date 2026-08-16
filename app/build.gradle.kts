plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

// O plugin google-services aborta o build quando não encontra o google-services.json, e esse
// arquivo não vai para o Git (repositório público — ver .gitignore). Aplicar condicionalmente
// mantém o build verde em clone novo e no CI; quando o arquivo aparece, o Firebase liga sozinho.
//
// O aviso é deliberadamente barulhento: build silenciosamente sem Firebase é pior que build que
// falha, porque o problema só aparece em runtime.
val firebaseConfigured = file("google-services.json").exists()

if (firebaseConfigured) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
    apply(plugin = libs.plugins.firebase.perf.get().pluginId)
} else {
    logger.warn(
        "AVISO: app/google-services.json não encontrado — Firebase desligado neste build. " +
            "Baixe o arquivo no console do Firebase — ver seção Firebase do README.",
    )
}

android {
    namespace = "com.gabrielfreire.runandlift"
    compileSdk {
        version = release(version = 37)
    }

    defaultConfig {
        applicationId = "com.gabrielfreire.runandlift"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Sem o google-services.json o plugin não roda, e `default_web_client_id` — que ele
        // geraria — não existe. Como RunAndLiftNavHost referencia esse R.string, o build quebrava
        // em compileDebugKotlin justamente onde o arquivo nunca está: o CI.
        //
        // O espaço reservado entra só quando o plugin está ausente; declarar sempre daria recurso
        // duplicado. Vazio de propósito: um build sem google-services.json não tem Firebase para
        // autenticar de qualquer forma, e valor falso plausível só adiaria o erro para o runtime.
        if (!firebaseConfigured) {
            resValue("string", "default_web_client_id", "")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
        debug {
            // O plugin de Performance instrumenta bytecode em tempo de build, e isso pesa em todo
            // ciclo de desenvolvimento. Em debug a coleta já está desligada pelo manifesto, então
            // instrumentar não serviria para nada. Guardado pelo `if` porque a extensão só existe
            // quando o plugin foi aplicado — ou seja, quando há google-services.json.
            if (firebaseConfigured) {
                configure<com.google.firebase.perf.plugin.FirebasePerfExtension> {
                    setInstrumentationEnabled(false)
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true

        // Desligado por padrão no AGP 9: sem isto, o `resValue` do defaultConfig falha na
        // configuração com "contains custom resource values, but the feature is disabled".
        resValues = true
    }
}

dependencies {
    // :core traz Compose e Material 3 por `api` — não redeclarar aqui.
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:student"))
    implementation(project(":feature:trainer"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Firebase de escopo do app: observabilidade e flags. Firestore e Auth ficam em :data, que é
    // quem fala com o backend.
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    lintChecks(libs.compose.lint.checks)

    testImplementation(libs.junit)
    // MainViewModel resolve o destino inicial numa corrotina do `viewModelScope`, que despacha na
    // Main — sem o dispatcher de teste não há como afirmar o resultado.
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
