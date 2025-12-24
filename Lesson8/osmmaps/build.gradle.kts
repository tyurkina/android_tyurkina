plugins {
    id("com.android.application")
}

android {
    namespace = "com.mirea.tyurkinaia.osmmaps"
    compileSdk = 34

    buildFeatures {
        dataBinding = true
    }
        defaultConfig {
        applicationId = "com.mirea.tyurkinaia.osmmaps"
        minSdk = 26
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.yandex.android:maps.mobile:4.6.1-full")
    implementation("androidx.core:core:1.12.0")
}