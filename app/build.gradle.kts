import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// local.properties'ten API anahtarını oku (android bloğu dışında)
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) localProps.load(localPropsFile.inputStream())
val geminiApiKey: String = localProps.getProperty("GEMINI_API_KEY", "")

android {
    namespace = "tr.edu.duzce.mf.bm.calorietrackingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "tr.edu.duzce.mf.bm.calorietrackingapp"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // WorkManager
    implementation(libs.work.runtime)

    // OkHttp - Gemini REST API çağrıları
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}