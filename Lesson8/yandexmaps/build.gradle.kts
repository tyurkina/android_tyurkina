plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mirea.tyurkinaia.yandexmaps"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.mirea.tyurkinaia.yandexmaps"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {

        implementation("androidx.compose.material3:material3:1.2.0-beta01")
        implementation("com.google.android.material:material:1.13.0")
        implementation("com.yandex.android:maps.mobile:4.3.1-full")
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
}