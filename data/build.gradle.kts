plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.application.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.poi.ooxml)
    implementation(libs.gson)
    implementation(libs.language.id)
    implementation(libs.nanohttpd)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
}