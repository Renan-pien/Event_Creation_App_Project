import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
}

// Assinatura de release: crie "keystore.properties" na RAIZ do projeto (arquivo
// NÃO versionado — já está no .gitignore) com o conteúdo:
//   storeFile=syncmeet-release.jks
//   storePassword=***
//   keyAlias=syncmeet
//   keyPassword=***
// Sem esse arquivo o build release funciona, mas o AAB sai sem assinatura.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.syncmeet.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.syncmeet.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL de PRODUÇÃO (HTTPS) — domínio oficial do backend.
        buildConfigField("String", "BASE_URL", "\"https://syncmeet.freedev.app/syncmeet/\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Emulador: 10.0.2.2 aponta para o localhost da máquina (XAMPP).
            // HTTP (cleartext) só é liberado neste build, via src/debug/AndroidManifest.xml.
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2/syncmeet/\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
