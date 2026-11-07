import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.openstream.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.openstream.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"

        buildConfigField(
            "String",
            "UPDATE_MANIFEST_URL",
            "\"https://raw.githubusercontent.com/mahmudjanovr/openStream/master/update-server/update.json\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePropFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            if (keystorePropFile.exists()) {
                keystorePropFile.inputStream().use { keystoreProperties.load(it) }
            }

            val storeFilePath = System.getenv("OPENSTREAM_KEYSTORE_FILE")
                ?: project.findProperty("OPENSTREAM_KEYSTORE_FILE") as? String
                ?: keystoreProperties.getProperty("storeFile")

            val storePwd = System.getenv("OPENSTREAM_STORE_PASSWORD")
                ?: project.findProperty("OPENSTREAM_STORE_PASSWORD") as? String
                ?: keystoreProperties.getProperty("storePassword")

            val keyAliasName = System.getenv("OPENSTREAM_KEY_ALIAS")
                ?: project.findProperty("OPENSTREAM_KEY_ALIAS") as? String
                ?: keystoreProperties.getProperty("keyAlias")

            val keyPwd = System.getenv("OPENSTREAM_KEY_PASSWORD")
                ?: project.findProperty("OPENSTREAM_KEY_PASSWORD") as? String
                ?: keystoreProperties.getProperty("keyPassword")

            if (storeFilePath != null &&
                !storePwd.isNullOrBlank() &&
                !keyAliasName.isNullOrBlank() &&
                !keyPwd.isNullOrBlank()
            ) {
                val resolvedFile = if (File(storeFilePath).isAbsolute) {
                    File(storeFilePath)
                } else {
                    rootProject.file(storeFilePath).takeIf { it.exists() } ?: file(storeFilePath)
                }
                if (resolvedFile.exists()) {
                    storeFile = resolvedFile
                    storePassword = storePwd
                    keyAlias = keyAliasName
                    keyPassword = keyPwd
                    enableV1Signing = true
                    enableV2Signing = true
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Networking & Serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    // AndroidX Media3 (ExoPlayer)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
