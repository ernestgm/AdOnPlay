plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.firebase.perf)
}

android {
    namespace = "com.geniusdevelops.adonplay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.geniusdevelops.adonplay"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            applicationIdSuffix = ".release"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    productFlavors {
        create("prod") {
            buildConfigField("String", "ENV", "\"Prod\"")
            buildConfigField("String", "BASE_URL", "\"\"")
            buildConfigField("String", "WS_BASE_URL", "\"\"")
            buildConfigField(
                "String", "PLAYER_BASE_URL",
                "\"http://10.0.2.2:3001\""
            )
            buildConfigField(
                "String", "PLAYER_DOMAIN",
                "\"10.0.2.2\""
            )
            dimension = "api"
        }
        create("desa") {
            buildConfigField("String", "ENV", "\"\"")
            buildConfigField(
                "String", "BASE_URL",
                "\"http://api-adonplay.geniusdevelops.com/api/v1/\""
            )
            buildConfigField(
                "String", "PLAYER_BASE_URL",
                "\"http://player-adonplay.geniusdevelops.com/\""
            )
            buildConfigField(
                "String", "PLAYER_DOMAIN",
                "\"player-adonplay.geniusdevelops.com/\""
            )
            buildConfigField(
                "String", "WS_BASE_URL",
                "\"ws://ws-adonplay.geniusdevelops.com/cable/\""
            )
            dimension = "api"
        }
        create("dev") {
            buildConfigField("String", "ENV", "\"\"")
            buildConfigField(
                "String", "BASE_URL",
                "\"http://10.0.2.2:9000/api/v1/\""
            )
            buildConfigField(
                "String", "PLAYER_BASE_URL",
                "\"http://10.0.2.2:3001\""
            )
            buildConfigField(
                "String", "PLAYER_DOMAIN",
                "\"10.0.2.2:3001\""
            )
            buildConfigField(
                "String", "WS_BASE_URL",
                "\"ws://10.0.2.2:9000/cable/\""
            )
            dimension = "api"
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this
            val project = "adonplay"
            val flavor = variant.productFlavors[0].name
            val versionName = variant.versionName
            val apkName = "${project}-${flavor}-${versionName}.apk"
            (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                apkName
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    flavorDimensions += listOf("api")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.converter.gson)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}