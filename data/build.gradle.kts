plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.gabrielfreire.runandlift.data"

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

    // Existe por uma razão só: o construtor de `FirebaseException` chama `TextUtils.isEmpty`, e sem
    // isto nenhuma exceção do SDK pode ser construída num teste de JVM — o que deixaria a tradução
    // de erro de autenticação (`AuthFailureMapping`) sem como ser afirmada.
    //
    // O preço: método do Android não mockado passa a devolver null/0/false em vez de lançar. Um
    // teste que encoste sem querer numa API do Android recebe resposta errada em silêncio, em vez
    // de erro alto. É por isso que os testes deste módulo continuam sendo sobre regra em Kotlin
    // puro; a alternativa era o Robolectric, que resolveria isto sem o efeito colateral e custaria
    // uma dependência e segundos por classe. O gatilho para trocar é o primeiro teste que precisar
    // de um comportamento do Android de verdade, e não de uma exceção construída.
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// O esquema exportado é versionado em data/schemas/. Sem ele não há como escrever nem testar
// migração (E0-13) — o Room não tem de onde saber qual era o esquema anterior.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)

    // O backend vive atrás desta fronteira: nenhum tipo do Firestore ou do Auth atravessa para
    // :app. A persistência offline do Firestore vem ligada por padrão no Android, que é a razão
    // de ele ter sido escolhido no lugar do Realtime Database.
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lê o JSON da API do IBGE. Runtime só — a leitura é pela API de árvore, então o plugin de
    // serialização não entra no projeto.
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.android)
    // Traz o `Task.await()`, que converte a API de callbacks do Firebase em suspend.
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    testImplementation(libs.junit)
}
