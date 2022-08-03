plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(Apps.compileSdkVersion)

    defaultConfig {
        applicationId = Apps.applicationId
        minSdkVersion(Apps.minSdkVersion)
        targetSdkVersion(Apps.targetSdkVersion)
        versionCode = Apps.code
        versionName = Apps.version
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "v${variant.mergedFlavor.versionName}_${variant.mergedFlavor.versionCode}.apk"
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/*")
    }

    lintOptions {
        isAbortOnError = false
    }
}

dependencies {
}