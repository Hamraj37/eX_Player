import java.io.File
import java.util.Properties
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val keystoreBase64: String? = System.getenv("KEYSTORE_BASE64")
    ?: localProperties.getProperty("KEYSTORE_BASE64")
    ?: project.findProperty("KEYSTORE_BASE64") as? String

val keystorePassword: String? = System.getenv("KEYSTORE_PASSWORD")
    ?: localProperties.getProperty("KEYSTORE_PASSWORD")
    ?: project.findProperty("KEYSTORE_PASSWORD") as? String

val keyAliasStr: String? = System.getenv("KEY_ALIAS")
    ?: localProperties.getProperty("KEY_ALIAS")
    ?: project.findProperty("KEY_ALIAS") as? String

val keyPasswordStr: String? = System.getenv("KEY_PASSWORD")
    ?: localProperties.getProperty("KEY_PASSWORD")
    ?: project.findProperty("KEY_PASSWORD") as? String

val ksFile: File? = if (!keystoreBase64.isNullOrEmpty()) {
    val decodedBytes = Base64.getDecoder().decode(keystoreBase64.trim().replace("\n", "").replace("\r", ""))
    val outputFile = file("${layout.buildDirectory.get().asFile}/keystore.jks")
    outputFile.parentFile.mkdirs()
    outputFile.writeBytes(decodedBytes)
    outputFile
} else {
    null
}

android {
    namespace = "com.hamraj37.explayer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.hamraj37.explayer"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("customSigning") {
            if (ksFile != null && ksFile.exists()) {
                storeFile = ksFile
                storePassword = keystorePassword
                keyAlias = keyAliasStr
                keyPassword = keyPasswordStr
            }
        }
    }

    buildTypes {
        debug {
            if (ksFile != null && ksFile.exists()) {
                signingConfig = signingConfigs.getByName("customSigning")
            }
        }
        release {
            if (ksFile != null && ksFile.exists()) {
                signingConfig = signingConfigs.getByName("customSigning")
            }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.glide)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
}
