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

    implementation(libs.kotlinx.coroutines.android)
    // Traz o `Task.await()`, que converte a API de callbacks do Firebase em suspend.
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    testImplementation(libs.junit)
}
