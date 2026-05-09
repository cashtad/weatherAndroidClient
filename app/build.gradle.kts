plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "zcu.cz.kiv.weatherapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "zcu.cz.kiv.weatherapp"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended:1.7.7")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Retrofit + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Token storage (encrypted)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Images (weather icons)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation(libs.androidx.ui)
}