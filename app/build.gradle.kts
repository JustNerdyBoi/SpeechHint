plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "ru.application.speechhint"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.application.speechhint"
        minSdk = 26
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.properties["KEYSTORE_FILE"] as String)
            storePassword = project.properties["KEYSTORE_PASSWORD"] as String
            keyAlias = project.properties["KEY_ALIAS"] as String
            keyPassword = project.properties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":di"))

    implementation(libs.dagger.hilt.android)
    annotationProcessor(libs.dagger.hilt.android.compiler)

    implementation(libs.zxing.android.embedded)

    implementation(platform("ru.rustore.sdk:bom:2025.05.02"))
    implementation("ru.rustore.sdk:billingclient")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}