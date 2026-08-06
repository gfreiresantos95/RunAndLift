plugins {
    alias(libs.plugins.android.library)
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

dependencies {
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)

    // O backend vive atrás desta fronteira: nenhum tipo do Firestore ou do Auth atravessa para
    // :app. A persistência offline do Firestore vem ligada por padrão no Android, que é a razão
    // de ele ter sido escolhido no lugar do Realtime Database.
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
}
